/*
 *
 * Headwind MDM: Open Source Android MDM Software
 * https://h-mdm.com
 *
 * Copyright (C) 2019 Headwind Solutions LLC (http://h-sms.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.hmdm.rest.resource;

import com.google.common.io.Files;
import com.hmdm.notification.PushService;
import com.hmdm.persistence.ApplicationDAO;
import com.hmdm.persistence.UnsecureDAO;
import com.hmdm.persistence.UserDAO;
import com.hmdm.persistence.domain.Application;
import com.hmdm.persistence.domain.ApplicationVersion;
import com.hmdm.persistence.domain.User;
import com.hmdm.rest.json.*;
import com.hmdm.security.SecurityContext;
import com.hmdm.util.*;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Singleton
@Path("/private/update")
public class UpdateResource {
    private String baseUrl;
    private String filesDirectory;
    private String protocol;
    private String customerDomain;
    private ApplicationDAO applicationDAO;
    private UnsecureDAO unsecureDAO;
    private StatsSender statsSender;
    private APKFileAnalyzer apkFileAnalyzer;

    private static final Logger logger = LoggerFactory.getLogger(UpdateResource.class);
    private static final String WEB_MANIFEST_FILE_NAME = "hmdm_web_update_manifest.txt";

    public UpdateResource() {
    }

    @Inject
    public UpdateResource(@Named("files.directory") String filesDirectory,
                          @Named("base.url") String baseUrl,
                          ApplicationDAO applicationDAO,
                          UnsecureDAO unsecureDAO,
                          StatsSender statsSender,
                          APKFileAnalyzer apkFileAnalyzer) {
        this.filesDirectory = filesDirectory;
        this.baseUrl = baseUrl;
        this.applicationDAO = applicationDAO;
        this.unsecureDAO = unsecureDAO;
        this.statsSender = statsSender;
        this.apkFileAnalyzer = apkFileAnalyzer;
        try {
            URL url = new URL(baseUrl);
            protocol = url.getProtocol();
            customerDomain = url.getHost();
        } catch (MalformedURLException e) {
            // We shouldn't be here!
            e.printStackTrace();
        }

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/check")
    public Response checkUpdates() {

        String manifestStr = null;
        try {
            if (!unsecureDAO.isSingleCustomer() && !SecurityContext.get().isSuperAdmin()) {
                throw new SecurityException("Only superadmin can check for updates");
            }

            URL url = new URL(UpdateSettings.MANIFEST_URL.replace("CUSTOMER_DOMAIN", customerDomain));
            logger.info("Checking for update: " + url.toString());
            manifestStr = FileUtil.downloadTextFile(url);

            JSONArray array = new JSONArray(manifestStr);
            List<UpdateEntry> allUpdates = new LinkedList<>();
            for (int i = 0; i < array.length(); i++) {
                UpdateEntry updateEntry = new UpdateEntry(array.getJSONObject(i));

                if (updateEntry.getPkg().equals(UpdateEntry.WEB_PKG)) {
                    processWebAppEntry(updateEntry);
                    allUpdates.add(updateEntry);
                } else if (updateEntry.getPkg().equals(UpdateEntry.LAUNCHER_PKG)) {
                    processLauncherAppEntry(updateEntry);
                    allUpdates.add(updateEntry);
                } else {
                    if (processMobileAppEntry(updateEntry)) {
                        logger.info("Secondary APK: " + updateEntry.getUrl());
                        allUpdates.add(updateEntry);
                    }
                }
            }

            return Response.OK(allUpdates);

        } catch (Exception e) {
            e.printStackTrace();
            return Response.ERROR();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response downloadUpdates(UpdateRequest request) {

        String webVersion = "";

        for (UpdateEntry app : request.getUpdates()) {
            if (app.getPkg().equals("web")) {
                webVersion = app.getCurrentVersion();
            }
            if (!app.isOutdated() || app.isUpdateDisabled()) {
                continue;
            }
            if (!app.isDownloaded()) {
                if (app.getPkg().equals("web")) {
                    if (!downloadWebApp(app)) {
                        app.setUpdateDisabled(true);
                        app.setUpdateDisableReason(UpdateEntry.DISABLED_DOWNLOAD);
                        continue;
                    }
                } else {
                    if (!downloadAPKVersion(app)) {
                        app.setUpdateDisabled(true);
                        app.setUpdateDisableReason(UpdateEntry.DISABLED_DOWNLOAD);
                        continue;
                    }
                }
                app.setDownloaded(true);
            }
            if (request.isUpdate() && !app.getPkg().equals("web")) {
                updateAppInConfig(app);
                app.setCurrentVersion(app.getVersion());
            }
        }

        if (request.isSendStats()) {
            statsSender.sendStats(UpdateSettings.STAT_URL, protocol, customerDomain, webVersion);
        }

        return Response.OK(request.getUpdates());
    }

    // Download the web app (using the authentification!) and create the manifest file
    private boolean downloadWebApp(UpdateEntry app) {
        InputStream inputStream = null;
        HttpURLConnection conn = null;
        try {
            URL url = new URL(app.getUrl());
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(30000);
            conn.setConnectTimeout(30000);
            conn.setUseCaches(false);
            conn.setAllowUserInteraction(false);
            conn.setRequestMethod("GET");

            if (UpdateSettings.WEB_UPDATE_USERNAME != null && UpdateSettings.WEB_UPDATE_PASSWORD != null) {
                String userCredentials = UpdateSettings.WEB_UPDATE_USERNAME + ":" + UpdateSettings.WEB_UPDATE_PASSWORD;
                String basicAuth = "Basic " + new String(Base64.getEncoder().encodeToString(userCredentials.getBytes()));
                conn.setRequestProperty("Authorization", basicAuth);
            }

            File file = new File(filesDirectory, getFileNameFromUrl(app.getUrl()));
            inputStream = conn.getInputStream();
            FileUtil.writeToFile(conn.getInputStream(), file.getAbsolutePath());

            createWebManifest(app);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.disconnect();
                } catch (Exception e) {
                    // Ignore
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    // Ignore
                }
            }
        }

        return true;
    }

    // Download APK file and create a new version
    // Returns true if the app was downloaded successfully and false on error
    private boolean downloadAPKVersion(UpdateEntry app) {
        try {
            String name = getFileNameFromUrl(app.getUrl());
            if (!name.endsWith(".apk") && !name.endsWith(".xapk")) {
                logger.warn("Can't update app - wrong URL; " + app.getUrl());
                return false;
            }

            List<Application> appList = applicationDAO.findByPackageId(app.getPkg());
            if (appList.size() != 1) {
                logger.warn("Failed to update app " + app.getPkg() + ": app not exists or multiple app entries");
                return false;
            }

            // We download to the temp file to reuse the existing Application management API
            File tempFile = FileUtil.createTempFile(FileUtil.adjustFileName(name));
            FileUtil.writeToFile(new URL(app.getUrl()).openStream(), tempFile.getAbsolutePath());

            APKFileDetails fileDetails = apkFileAnalyzer.analyzeFile(tempFile.getAbsolutePath());

            ApplicationVersion version = new ApplicationVersion();
            version.setFilePath(tempFile.getAbsolutePath());
            version.setApplicationId(appList.get(0).getId());
            version.setVersion(fileDetails.getVersion());
            version.setVersionCode(fileDetails.getVersionCode());
            applicationDAO.insertApplicationVersion(version);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private void createWebManifest(UpdateEntry app) throws IOException {
        File appFile = new File(filesDirectory, getFileNameFromUrl(app.getUrl()));
        File manifestFile = new File(filesDirectory, WEB_MANIFEST_FILE_NAME);
        if (manifestFile.exists()) {
            manifestFile.delete();
        }

        Files.write(appFile.getAbsolutePath().getBytes(StandardCharsets.UTF_8), manifestFile);
    }

    // Update installedVersion to version in all configs
    // Record which configurations are affected to notify them via Push notification
    private void updateAppInConfig(UpdateEntry app) {
        ApplicationVersion currentVersion = applicationDAO.findApplicationVersion(app.getPkg(), app.getCurrentVersion());
        ApplicationVersion newVersion = applicationDAO.findApplicationVersion(app.getPkg(), app.getVersion());

        LinkConfigurationsToAppVersionRequest request = new LinkConfigurationsToAppVersionRequest();
        request.setApplicationVersionId(newVersion.getId());
        List<ApplicationVersionConfigurationLink> linkList = applicationDAO.getApplicationVersionConfigurations(currentVersion.getId());
        for (ApplicationVersionConfigurationLink link : linkList) {
            link.setApplicationVersionId(newVersion.getId());
        }
        request.setConfigurations(linkList);
        logger.info("Application versions updated by the superadmin through 'Check for updates'");
        applicationDAO.updateApplicationVersionConfigurations(request, SecurityContext.get().getCurrentUser().get());
    }

    private void processWebAppEntry(UpdateEntry entry) {
        if (!UpdateSettings.WEB_UPDATE_ENABLED) {
            entry.setUpdateDisabled(true);
            entry.setUpdateDisableReason(UpdateEntry.DISABLED_CUSTOM);
            return;
        }

        entry.setUrl(entry.getUrl().replace("WEB_SUFFIX", UpdateSettings.WEB_SUFFIX));
        logger.info("Web app: " + entry.getUrl());

        // Check if the file is downloaded
        File file = new File(filesDirectory, getFileNameFromUrl(entry.getUrl()));
        entry.setDownloaded(file.exists());

        entry.setName("Web panel");

        // For the web app, we set the current version and name on the front end
    }

    private void processLauncherAppEntry(UpdateEntry entry) {
        // We need to determine the currently installed launcher variant (os/master/system or custom)
        List<Application> appList = applicationDAO.findByPackageId(entry.getPkg());
        if (appList.size() != 1) {
            // Not available for update
            entry.setUpdateDisabled(true);
            if (appList.size() == 0) {
                // In a multi-tenant setup, we shouldn't be here because updates are disabled
                entry.setUpdateDisableReason(UpdateEntry.DISABLED_NOT_MASTER);
            } else {
                entry.setName(appList.get(0).getName());
                entry.setUpdateDisableReason(UpdateEntry.DISABLED_MULTIPLE);
            }
            return;
        }

        entry.setName(appList.get(0).getName());
        List<ApplicationVersion> versions = applicationDAO.getApplicationVersions(appList.get(0).getId());
        if (versions.size() == 0) {
            // We shouldn't be here!
            entry.setUpdateDisabled(true);
            entry.setUpdateDisableReason(UpdateEntry.DISABLED_ERROR);
            return;
        }

        ApplicationVersion currentVersion = findLatestInstalledVersion(versions);
        if (currentVersion == null) {
            // For launcher, we shouldn't be here
            entry.setUpdateDisabled(true);
            entry.setUpdateDisableReason(UpdateEntry.DISABLED_ERROR);
            return;
        }
        entry.setCurrentVersion(currentVersion.getVersion());

        // Determine the launcher variant by file name of the latest version
        // We presume that the name is hmdm-x.xx.x-suffix.apk
        if (currentVersion.getUrl() == null) {
            entry.setUpdateDisabled(true);
            entry.setUpdateDisableReason(UpdateEntry.DISABLED_CUSTOM);
            return;
        }
        String name = getFileNameFromUrl(currentVersion.getUrl());
        if (!name.endsWith(".apk") && !name.endsWith(".xapk")) {
            entry.setUpdateDisabled(true);
            entry.setUpdateDisableReason(UpdateEntry.DISABLED_CUSTOM);
            return;
        }

        name = name.substring(0, name.length() - 4);
        String[] parts = name.split("-");
        if (parts.length != 3) {
            entry.setUpdateDisabled(true);
            entry.setUpdateDisableReason(UpdateEntry.DISABLED_CUSTOM);
            return;
        }

        if (!(parts[2].equals("os") || parts[2].equals("master") || parts[2].equals("system"))) {
            entry.setUpdateDisabled(true);
            entry.setUpdateDisableReason(UpdateEntry.DISABLED_CUSTOM);
            return;
        }

        // Use corresponding APK URL from the manifest
        entry.setUrl(entry.getUrl().replace("LAUNCHER_SUFFIX", parts[2]));
        logger.info("Launcher APK: " + entry.getUrl());

        File file = new File(filesDirectory, getFileNameFromUrl(entry.getUrl()));
        entry.setDownloaded(file.exists());
    }

    private boolean processMobileAppEntry(UpdateEntry entry) {
        List<Application> appList = applicationDAO.findByPackageId(entry.getPkg());
        if (appList.size() != 1) {
            // We only update automatically in a simple case (one application)
            return false;
        }

        List<ApplicationVersion> versions = applicationDAO.getApplicationVersions(appList.get(0).getId());
        if (versions.size() == 0) {
            // We shouldn't be here!
            return false;
        }

        ApplicationVersion installedVersion = findLatestInstalledVersion(versions);
        if (installedVersion == null) {
            // App not installed in configurations so no need to update
            return false;
        }
        entry.setCurrentVersion(installedVersion.getVersion());

        File file = new File(filesDirectory, getFileNameFromUrl(entry.getUrl()));
        entry.setDownloaded(file.exists());

        entry.setName(appList.get(0).getName());

        return true;
    }

    private ApplicationVersion findLatestVersion(List<ApplicationVersion> versions) {
        return Collections.max(versions, (o1, o2) -> ApplicationUtil.compareVersions(o1.getVersion(), o2.getVersion()));
    }

    private ApplicationVersion findLatestInstalledVersion(List<ApplicationVersion> versions) {
        ApplicationVersion result = null;
        for (ApplicationVersion v : versions) {
            if (v.isDeletionProhibited() && (result == null || ApplicationUtil.compareVersions(v.getVersion(), result.getVersion()) > 0)) {
                result = v;
            }
        }
        return result;
    }

    private String getFileNameFromUrl(String url) {
        int pos = url.lastIndexOf('/');
        return url.substring(pos + 1);
    }
}
