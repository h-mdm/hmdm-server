package com.hmdm.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class RESTUtil {
    public static String send(String location, String method, String auth, String body) {
        HttpURLConnection conn = null;
        try {
            URL url = URI.create(location).toURL();
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(30000);
            conn.setConnectTimeout(30000);
            conn.setUseCaches(false);
            conn.setAllowUserInteraction(false);
            conn.setRequestMethod(method);
            if (auth != null) {
                conn.setRequestProperty("Authorization", auth);
            }
            if (body != null) {
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");
            }
            conn.connect();

            if (body != null) {
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = body.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
            }

            try (BufferedReader br =
                    new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                return response.toString();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                conn.disconnect();
            } catch (Exception e) {
            }
        }
    }
}
