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

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.inject.Named;

import com.hmdm.persistence.CustomerDAO;
import com.hmdm.persistence.domain.*;
import com.hmdm.rest.json.Response;
import com.hmdm.security.SecurityContext;
import com.hmdm.util.StringUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;
import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.javase.QRCode;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hmdm.persistence.UnsecureDAO;
import com.hmdm.util.CryptoUtil;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * <p>A resource used for returning the QR-code for the requested configuration.</p>
 *
 * @author isv
 */
@Api(tags = {"QR-code"})
@Singleton
@Path("/public/qr")
public class QRCodeResource {

    private static final Logger logger = LoggerFactory.getLogger(QRCodeResource.class);

    private UnsecureDAO unsecureDAO;
    private CustomerDAO customerDAO;

    private String filesDirectory;
    private String baseUrlForQrCode;

    /**
     * <p>A constructor required by Swagger.</p>
     */
    public QRCodeResource() {
    }

    /**
     * <p>Constructs new <code>QRCodeResource</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public QRCodeResource(UnsecureDAO unsecureDAO,
                          CustomerDAO customerDAO,
                          @Named("files.directory") String filesDirectory,
                          @Named("base.url") String baseUrl) throws MalformedURLException {
        this.unsecureDAO = unsecureDAO;
        this.customerDAO = customerDAO;
        this.filesDirectory = filesDirectory;
        final URL url = new URL(baseUrl);
        final int port = url.getPort();
        this.baseUrlForQrCode = url.getProtocol() + "://" + url.getHost() + (port != -1 ? ":" + port : "");
    }

    /**
     * <p>Gets the QR code image for the specified configuration.</p>
     *
     * @param id a QR code key referencing the configuration.
     * @return a response to client providing the QR code image.
     */
    // =================================================================================================================
    @ApiOperation(
            value = "Get a JSON",
            notes = "Gets the JSON for the specified configuration.",
            responseHeaders = {@ResponseHeader(name = "Content-Type")}
    )
    @ApiResponses({
            @ApiResponse(code = 500, message = "Internal server error"),
    })
    @GET
    @Path("/json/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public javax.ws.rs.core.Response generateJSON(@PathParam("id") @ApiParam("Configuration ID") String id,
                                                    @QueryParam("deviceId") @ApiParam("A device ID") String deviceID,
                                                    @QueryParam("create") @ApiParam("Create on demand") String createOnDemand,
                                                    @QueryParam("useId") @ApiParam("Which parameter to use as a device ID") String useId,
                                                    @QueryParam("group") @ApiParam("Groups to assign when creating a device") List<String> groups,
                                                    @Context HttpServletRequest req) {
        logger.info("Generating JSON for configuration key: {}", id);
        try {
            Configuration configuration = this.unsecureDAO.getConfigurationByQRCodeKey(id);
            if (configuration != null) {
                String res = generateExtrasBundle(deviceID, createOnDemand, configuration, groups, useId, req.getContextPath());
                return javax.ws.rs.core.Response.ok(res).build();
            } else {
                logger.error("Configuration not found for key: {}", id);
                return javax.ws.rs.core.Response.serverError().build();
            }

        } catch (Exception e) {
            logger.error("Unexpected error while generating the QR-code image", e);
            return javax.ws.rs.core.Response.serverError().build();
        }
    }

    /**
     * <p>Gets the QR code image for the specified configuration.</p>
     *
     * @param id a QR code key referencing the configuration.
     * @param size an optional request parameter specifying the size of the image to be generated.
     * @return a response to client providing the QR code image.
     */
    // =================================================================================================================
    @ApiOperation(
            value = "Get QR-code",
            notes = "Gets the QR code image for the specified configuration.",
            responseHeaders = {@ResponseHeader(name = "Content-Type")}
    )
    @ApiResponses({
            @ApiResponse(code = 500, message = "Internal server error"),
    })
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public javax.ws.rs.core.Response generateQRCode(@PathParam("id") @ApiParam("Configuration ID") String id,
                                                    @QueryParam("size") @ApiParam("A size of the QR-code image") Integer size,
                                                    @QueryParam("deviceId") @ApiParam("A device ID") String deviceID,
                                                    @QueryParam("create") @ApiParam("Create on demand") String createOnDemand,
                                                    @QueryParam("useId") @ApiParam("Which parameter to use as a device ID") String useId,
                                                    @QueryParam("group") @ApiParam("Groups to assign when creating a device") List<String> groups,
                                                    @Context HttpServletRequest req) {
        logger.info("Generating QR-code image for configuration key: {}", id);
        try {
            Configuration configuration = this.unsecureDAO.getConfigurationByQRCodeKey(id);
            if (configuration != null) {
                Integer mainAppId = configuration.getMainAppId();
                if (mainAppId != null) {
                    ApplicationVersion appVersion = this.unsecureDAO.findApplicationVersionById(mainAppId);
                    if (appVersion != null && appVersion.getUrl() != null && !appVersion.getUrl().trim().isEmpty()) {
                        final String apkUrl = appVersion.getUrl().replace(" ", "%20");
                        final String sha256;
                        if (appVersion.getApkHash() == null) {
                            sha256 = calculateApkHash(apkUrl);
                            this.unsecureDAO.saveApkFileHash(appVersion.getId(), sha256);
                        } else {
                            sha256 = appVersion.getApkHash();
                        }

                        Application appMain = this.unsecureDAO.findApplicationById(appVersion.getApplicationId());

                        String wifiSsidEntry = "";
                        if (configuration.getWifiSSID() != null && !configuration.getWifiSSID().trim().isEmpty()) {
                            String wifiSecurityType = configuration.getWifiSecurityType();
                            if (wifiSecurityType == null || wifiSecurityType.isEmpty()) {
                                wifiSecurityType = "WPA";   // De-facto standard
                            }
                            wifiSsidEntry = "\"android.app.extra.PROVISIONING_WIFI_SSID\":" + JSONObject.quote(configuration.getWifiSSID().trim()) + ",\n" +
                                            "\"android.app.extra.PROVISIONING_WIFI_SECURITY_TYPE\":\"" + wifiSecurityType + "\",\n";
                        }

                        String wifiPasswordEntry = "";
                        if (configuration.getWifiPassword() != null && !configuration.getWifiPassword().trim().isEmpty()) {
                            wifiPasswordEntry = "\"android.app.extra.PROVISIONING_WIFI_PASSWORD\":" + JSONObject.quote(configuration.getWifiPassword().trim()) + ",\n";
                        }

                        String mobileEnrollmentEntry = "";
                        if (configuration.isMobileEnrollment()) {
                            mobileEnrollmentEntry = "\"android.app.extra.PROVISIONING_USE_MOBILE_DATA\":true,\n";
                        }

                        String miscQrParametersEntry = "";
                        if (configuration.getQrParameters() != null) {
                            miscQrParametersEntry = configuration.getQrParameters().trim();
                            if (!miscQrParametersEntry.equals("")) {
                                if (!miscQrParametersEntry.endsWith(",")) {
                                    miscQrParametersEntry += ",";
                                }
                                miscQrParametersEntry += "\n";
                            }
                        }

                        StringBuffer sb = new StringBuffer("{\n" +
                                "\"android.app.extra.PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME\":\"" + appMain.getPkg() +"/" + configuration.getEventReceivingComponent() + "\",\n" +
                                "\"android.app.extra.PROVISIONING_DEVICE_ADMIN_PACKAGE_DOWNLOAD_LOCATION\":" + JSONObject.quote(apkUrl) + ",\n" +
                                "\"android.app.extra.PROVISIONING_DEVICE_ADMIN_PACKAGE_CHECKSUM\":\"" + sha256 + "\",\n" +
                                wifiSsidEntry + wifiPasswordEntry + mobileEnrollmentEntry +
                                "\"android.app.extra.PROVISIONING_LEAVE_ALL_SYSTEM_APPS_ENABLED\":true,\n");
                        if (!configuration.isEncryptDevice()) {
                            sb.append("\"android.app.extra.PROVISIONING_SKIP_ENCRYPTION\":true,\n");
                        }
                        sb.append(miscQrParametersEntry +
                                "\"android.app.extra.PROVISIONING_ADMIN_EXTRAS_BUNDLE\": " +
                                generateExtrasBundle(deviceID, createOnDemand, configuration, groups, useId, req.getContextPath()) +
                                "}\n");
                        final String s = sb.toString();

                        logger.info("The base for QR code generation:\n{}", s);

                        return javax.ws.rs.core.Response.ok( (StreamingOutput) output -> {
                            int imageSize = 250;
                            if (size != null) {
                                imageSize = size;
                            }
                            try {
                                QRCode.from(s).to(ImageType.PNG).withSize(imageSize, imageSize).writeTo(output);
                                output.flush();
                            } catch ( Exception e ) { e.printStackTrace(); }
                        } )
                                .header("Cache-Control", "no-cache")
                                .header( "Content-Type", "image/png" ).build();

                    } else {
                        logger.info("Main app for configuration for QR-code key {} does not have URL set", id);
                    }
                } else {
                    logger.info("Configuration for QR-code key {} does not have a main app set", id);
                }
            } else {
                logger.info("Configuration not found for QR-code key: {}", id);
            }

            return javax.ws.rs.core.Response.ok().build();

        } catch (Exception e) {
            logger.error("Unexpected error while generating the QR-code image", e);
            return javax.ws.rs.core.Response.serverError().build();
        }
    }

    private String calculateApkHash(String apkUrl) throws NoSuchAlgorithmException, IOException {
        logger.info("Digesting the application file: {}", apkUrl);

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] buffer= new byte[8192];
        int count;

        URL url = new URL(apkUrl);
        if (apkUrl.startsWith(baseUrlForQrCode + "/files/")) {
            // Local URL, use the file system
            String urlPath = url.getPath();
            int index = urlPath.indexOf("/files/", 0) + "/files/".length();
            String path = urlPath.substring(index);
            File file = new File(String.format("%s/%s", this.filesDirectory, path));
            if (file.exists()) {
                try (InputStream input = new FileInputStream(file)) {
                    while ((count = input.read(buffer)) > 0) {
                        digest.update(buffer, 0, count);
                    }
                }
            } else {
                throw new FileNotFoundException();
            }
        } else {
            // Remote URL, use HTTP
            try (BufferedInputStream bis = new BufferedInputStream(url.openStream())) {
                while ((count = bis.read(buffer)) > 0) {
                    digest.update(buffer, 0, count);
                }
            }
        }

        final byte[] hash = digest.digest();
        String sha256 = CryptoUtil.getBase64String(hash);

        logger.info("Finished digesting the application file: {}. Hash: {}", apkUrl, sha256);
        return sha256;
    }

    private String generateExtrasBundle(String deviceID, String createOnDemand, Configuration configuration,
                                        List<String> groups, String useId, String contextPath) {

        if (contextPath.startsWith("/")) {
            contextPath = contextPath.substring(1);
        }

        String deviceIdEntry = "";
        if (deviceID != null && !deviceID.trim().isEmpty()) {
            deviceID = deviceID.trim();
            deviceIdEntry = "\"com.hmdm.DEVICE_ID\":\"" + deviceID + "\",";
        }


        String configurationEntry = "";
        String customerEntry = "";
        if (createOnDemand != null && createOnDemand.equals("1")) {
            configurationEntry = "\"com.hmdm.CONFIG\":\"" + Integer.toString(configuration.getId()) + "\",\n";

            if (!unsecureDAO.isSingleCustomer()) {
                Customer customer = customerDAO.findById(configuration.getCustomerId());
                customerEntry = "\"com.hmdm.CUSTOMER\":\"" + StringUtil.jsonEscape(customer.getName()) + "\",\n";
            }
        }

        String groupEntry = "";
        if (groups != null && groups.size() > 0) {
            groupEntry = "\"com.hmdm.GROUP\":\"";
            boolean needComma = false;
            for (String group : groups) {
                if (needComma) {
                    groupEntry += ",";
                } else {
                    needComma = true;
                }
                groupEntry += group;
            }
            groupEntry += "\",\n";
        }

        String useIdEntry = "";
        if (useId != null) {
            useIdEntry = "\"com.hmdm.DEVICE_ID_USE\":\"" + StringUtil.jsonEscape(useId) + "\",\n";
        }


        String bundle = "{" +
                deviceIdEntry +
                configurationEntry +
                customerEntry +
                useIdEntry +
                groupEntry +
                "\"com.hmdm.BASE_URL\":\"" + this.baseUrlForQrCode + "\",\n" +
                "\"com.hmdm.SERVER_PROJECT\":\"" + contextPath + "\"" +
                "}\n";
        return bundle;
    }

}
