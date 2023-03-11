package com.hmdm.util;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hmdm.persistence.DeviceDAO;
import com.hmdm.persistence.domain.UsageStats;

import java.io.*;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

@Singleton
public class StatsSender {

    private final DeviceDAO deviceDAO;

    @Inject
    public StatsSender(DeviceDAO deviceDAO) {
        this.deviceDAO = deviceDAO;
    }

    public void sendStats(String statUrl, String protocol, String customerDomain, String webVersion) {
        UsageStats stats = new UsageStats();
        stats.setTs(System.currentTimeMillis());
        stats.setInstanceId(CryptoUtil.getMD5String(customerDomain));
        stats.setWebVersion(webVersion);
        stats.setCommunity(UpdateSettings.WEB_UPDATE_USERNAME == null);
        stats.setDevicesTotal((int)deviceDAO.getTotalDevicesCount());
        stats.setDevicesOnline((int)deviceDAO.getOnlineDevicesCount());

        try {
            OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();
            String arch = bean.getArch();
            int cpuCount = bean.getAvailableProcessors();
            double loadAverage = bean.getSystemLoadAverage();

            MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
            MemoryUsage memoryUsage = memoryMXBean.getHeapMemoryUsage();
            long max = memoryUsage.getMax();
            long used = memoryUsage.getUsed();

            stats.setArch(arch);
            stats.setCpuTotal(cpuCount);
            stats.setCpuUsed((int)(loadAverage * 100.0));
            stats.setRamTotal((int)(max / 1048576l));
            stats.setRamUsed((int)(used / 1048576l));
        } catch (Exception e) {
            e.printStackTrace();
        }

        stats.setScheme(protocol);
        stats.setOs(System.getProperty("os.name"));
        if (stats.getOs().equalsIgnoreCase("Linux")) {
            // For Linux, we can get info from /etc/os-release
            String linuxVersion = getLinuxVersion();
            if (linuxVersion != null) {
                stats.setOs(linuxVersion);
            }
        }

        HttpURLConnection conn = null;
        try {
            URL url = new URL(statUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(30000);
            conn.setConnectTimeout(30000);
            conn.setUseCaches(false);
            conn.setAllowUserInteraction(false);
            conn.setDoOutput(true);
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.connect();

            try(OutputStream os = conn.getOutputStream()) {
                String jsonString = stats.toJsonString();
                byte[] input = jsonString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            try(BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                conn.disconnect();
            } catch (Exception e) {
            }
        }
    }

    private String getLinuxVersion() {
        FileReader fr = null;
        try {
            File file = new File("/etc/os-release");
            fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null) {
                // All lines have the form PROPERTY=VALUE
                String[] parts = line.split("=");
                if (parts.length == 2 && parts[0].equals("PRETTY_NAME")) {
                    // Unquote if necessary
                    return parts[1].startsWith("\"") ? parts[1].substring(1, parts[1].length() - 1) : parts[1];
                }
            }
        } catch (Exception e) {
            return null;
        } finally {
            if (fr != null) {
                try {
                    fr.close();
                } catch (Exception e) {
                }
            }
        }
        return null;
    }
}
