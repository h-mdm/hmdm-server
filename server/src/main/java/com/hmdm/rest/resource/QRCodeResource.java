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
import com.hmdm.persistence.domain.ApplicationVersion;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;
import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.javase.QRCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hmdm.persistence.UnsecureDAO;
import com.hmdm.persistence.domain.Application;
import com.hmdm.persistence.domain.Configuration;
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
import java.io.BufferedInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;

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

    private UnsecureDAO configurationDAO;

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
    public QRCodeResource(UnsecureDAO configurationDAO,
                          @Named("base.url") String baseUrl) throws MalformedURLException {
        this.configurationDAO = configurationDAO;
        final URL url = new URL(baseUrl);
        final int port = url.getPort();
        this.baseUrlForQrCode = url.getProtocol() + "://" + url.getHost() + (port != -1 ? ":" + port : "");
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
                                                    @Context HttpServletRequest req) {
        logger.info("Generating QR-code image for configuration key: {}", id);
        try {
            Configuration configuration = this.configurationDAO.getConfigurationByQRCodeKey(id);
            if (configuration != null) {
                Integer mainAppId = configuration.getMainAppId();
                if (mainAppId != null) {
                    ApplicationVersion appVersion = this.configurationDAO.findApplicationVersionById(mainAppId);
                    if (appVersion != null && appVersion.getUrl() != null && !appVersion.getUrl().trim().isEmpty()) {
                        final String apkUrl = appVersion.getUrl().replace(" ", "%20");
                        final String sha256;
                        if (appVersion.getApkHash() == null) {
                            logger.info("Digesting the application file: {}", apkUrl);

                            MessageDigest digest = MessageDigest.getInstance("SHA-256");
                            byte[] buffer= new byte[8192];
                            int count;
                            try (BufferedInputStream bis = new BufferedInputStream(new URL(apkUrl).openStream())) {
                                while ((count = bis.read(buffer)) > 0) {
                                    digest.update(buffer, 0, count);
                                }
                            }

                            final byte[] hash = digest.digest();
                            sha256 = CryptoUtil.getBase64String(hash);

                            logger.info("Finished digesting the application file: {}. Hash: {}", apkUrl, sha256);

                            this.configurationDAO.saveApkFileHash(appVersion.getId(), sha256);
                        } else {
                            sha256 = appVersion.getApkHash();
                        }


                        final String s;
                        String contextPath = req.getContextPath();
                        if (contextPath.startsWith("/")) {
                            contextPath = contextPath.substring(1);
                        }

                        Application appMain = this.configurationDAO.findApplicationById(appVersion.getApplicationId());

                        String deviceIdEntry = "";
                        if (deviceID != null && !deviceID.trim().isEmpty()) {
                            deviceID = deviceID.trim();
                            deviceIdEntry = "\"com.hmdm.DEVICE_ID\":\"" + deviceID + "\",";
                        }

                        String wifiSsidEntry = "";
                        if (configuration.getWifiSSID() != null && !configuration.getWifiSSID().trim().isEmpty()) {
                            String wifiSecurityType = configuration.getWifiSecurityType();
                            if (wifiSecurityType == null || wifiSecurityType.isEmpty()) {
                                wifiSecurityType = "WPA";   // De-facto standard
                            }
                            wifiSsidEntry = "\"android.app.extra.PROVISIONING_WIFI_SSID\":\"" + configuration.getWifiSSID().trim() + "\",\n" +
                                            "\"android.app.extra.PROVISIONING_WIFI_SECURITY_TYPE\":\"" + wifiSecurityType + "\",\n";
                        }

                        String wifiPasswordEntry = "";
                        if (configuration.getWifiPassword() != null && !configuration.getWifiPassword().trim().isEmpty()) {
                            wifiPasswordEntry = "\"android.app.extra.PROVISIONING_WIFI_PASSWORD\":\"" + configuration.getWifiPassword().trim() + "\",\n";
                        }

                        s = "{\n" +
                                "\"android.app.extra.PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME\":\"" + appMain.getPkg() +"/" + configuration.getEventReceivingComponent() + "\",\n" +
                                "\"android.app.extra.PROVISIONING_DEVICE_ADMIN_PACKAGE_DOWNLOAD_LOCATION\":\"" + apkUrl + "\",\n" +
                                "\"android.app.extra.PROVISIONING_DEVICE_ADMIN_PACKAGE_CHECKSUM\":\"" + sha256 + "\",\n" +
                                wifiSsidEntry + wifiPasswordEntry +
                                "\"android.app.extra.PROVISIONING_LEAVE_ALL_SYSTEM_APPS_ENABLED\":true,\n" +
                                "\"android.app.extra.PROVISIONING_SKIP_ENCRYPTION\":true,\n" +
                                "\"android.app.extra.PROVISIONING_ADMIN_EXTRAS_BUNDLE\": " +
                                  "{" +
                                    deviceIdEntry +
                                    "\"com.hmdm.BASE_URL\":\"" + this.baseUrlForQrCode + "\"," +
                                    "\"com.hmdm.SERVER_PROJECT\":\"" + contextPath + "\"" +
                                  "}\n" +
                                "}\n";

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

}
