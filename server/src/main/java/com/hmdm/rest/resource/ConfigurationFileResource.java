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

import com.hmdm.persistence.CustomerDAO;
import com.hmdm.persistence.UnsecureDAO;
import com.hmdm.persistence.UploadedFileDAO;
import com.hmdm.persistence.domain.Customer;
import com.hmdm.persistence.domain.UploadedFile;
import com.hmdm.rest.json.FileUploadResult;
import com.hmdm.rest.json.Response;
import com.hmdm.security.SecurityContext;
import com.hmdm.security.SecurityException;
import com.hmdm.util.CryptoUtil;
import com.hmdm.util.FileUtil;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * <p>A resource used for uploading configuration files to server.</p>
 */
@Singleton
@Path("/private/config-files")
public class ConfigurationFileResource {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationFileResource.class);

    private CustomerDAO customerDAO;
    private UnsecureDAO unsecureDAO;
    private String filesDirectory;
    private UploadedFileDAO uploadedFileDAO;
    private String baseUrl;

    /**
     * <p>Constructs new <code>ConfigurationFileResource</code> instance.</p>
     */
    public ConfigurationFileResource() {
        // Required by Swagger
    }

    /**
     * <p>Constructs new <code>ConfigurationFileResource</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public ConfigurationFileResource(CustomerDAO customerDAO,
                                     @Named("files.directory") String filesDirectory,
                                     @Named("base.url") String baseUrl,
                                     UploadedFileDAO uploadedFileDAO,
                                     UnsecureDAO unsecureDAO) {
        this.customerDAO = customerDAO;
        this.filesDirectory = filesDirectory;
        this.uploadedFileDAO = uploadedFileDAO;
        this.unsecureDAO = unsecureDAO;
        this.baseUrl = baseUrl;
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Upload configuration file",
            notes = "Uploads the configuration file to server. Returns a path to uploaded file",
            response = FileUploadResult.class
    )
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadConfigurationFile(@FormDataParam("file") InputStream uploadedInputStream,
                                            @ApiParam("A configuration file to upload") @FormDataParam("file")
                                                    FormDataContentDisposition fileDetail) {
        try {

            return SecurityContext.get().getCurrentCustomerId().map(customerId -> {
                try {
                    final Customer customer = this.customerDAO.findById(customerId);
                    final String customerFilesDir = customer.getFilesDir();

                    final File customerFilesDirectory = new File(this.filesDirectory, customerFilesDir);
                    if (!customerFilesDirectory.exists()) {
                        customerFilesDirectory.mkdirs();
                    }
                    // For some reason, the browser sends the file name in ISO_8859_1, so we use a workaround to convert
                    // it to UTF_8 and enable non-ASCII characters
                    // https://stackoverflow.com/questions/50582435/jersey-filename-encoded
                    String fileName = new String(fileDetail.getFileName().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                    File configFile = new File(customerFilesDirectory, fileName);

                    if (configFile.exists()) {
                        logger.warn("The file already exists and will be overwritten: {}", configFile.getAbsolutePath());
//                        return Response.FILE_EXISTS();
                    }

                    FileOutputStream fos = new FileOutputStream(configFile);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    IOUtils.copy(uploadedInputStream, bos);
                    uploadedInputStream.close();
                    bos.close();
                    fos.close();

                    if (!unsecureDAO.isSingleCustomer()) {
                        // Check the disk size in multi-tenant mode
                        if (!customer.isMaster() && customer.getSizeLimit() > 0) {
                            long userDirSize = 0;
                            long uploadFileSize = configFile.length();
                            userDirSize = FileUtils.sizeOfDirectory(customerFilesDirectory);
                            long totalSizeMb = (userDirSize + uploadFileSize) / 1048576l;
                            if (totalSizeMb > customer.getSizeLimit()) {
                                configFile.delete();
                                return Response.ERROR("error.size.limit.exceeded",
                                        "" + totalSizeMb + " / " + customer.getSizeLimit());
                            }
                        }
                    }

                    UploadedFile uploadedFile = new UploadedFile();
                    uploadedFile.setCustomerId(customerId);
                    uploadedFile.setFilePath(configFile.getName());
                    uploadedFile.setUploadTime(System.currentTimeMillis());

                    uploadedFile = this.uploadedFileDAO.insert(uploadedFile);

                    // Calculate checksum
                    final String checksum = CryptoUtil.calculateChecksum(Files.newInputStream(configFile.toPath()));
                    uploadedFile.setChecksum(checksum);
                    final String url = FileUtil.createFileUrl(this.baseUrl, customer.getFilesDir(), configFile.getName());
                    uploadedFile.setUrl(url);

                    return Response.OK(uploadedFile);

                } catch (Exception e) {
                    logger.error("Unexpected error when handling icon file upload", e);
                    return Response.INTERNAL_ERROR();
                }

            }).orElseThrow(SecurityException::onAnonymousAccess);

        } catch (Exception e) {
            logger.error("Unexpected error when handling icon file upload", e);
            return Response.INTERNAL_ERROR();
        }
    }

}
