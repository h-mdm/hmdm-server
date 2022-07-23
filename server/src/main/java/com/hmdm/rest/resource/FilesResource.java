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

import com.hmdm.persistence.ConfigurationFileDAO;
import com.hmdm.persistence.IconDAO;
import com.hmdm.persistence.domain.ApplicationVersion;
import com.hmdm.rest.json.APKFileDetails;
import com.hmdm.rest.json.FileUploadResult;
import com.hmdm.util.APKFileAnalyzer;
import com.hmdm.util.StringUtil;
import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.ResponseHeader;
import org.apache.poi.util.IOUtils;
import org.reflections.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hmdm.persistence.ApplicationDAO;
import com.hmdm.persistence.CustomerDAO;
import com.hmdm.persistence.domain.Application;
import com.hmdm.persistence.domain.Customer;
import com.hmdm.persistence.domain.HFile;
import com.hmdm.rest.json.MoveFileRequest;
import com.hmdm.rest.json.Response;
import com.hmdm.security.SecurityContext;
import com.hmdm.util.FileExistsException;
import com.hmdm.util.FileUtil;

import static com.hmdm.util.FileUtil.writeToFile;

@Api(tags = {"Files"}, authorizations = {@Authorization("Bearer Token")})
@Singleton
@Path("/private/web-ui-files")
public class FilesResource {
    private static final Logger logger = LoggerFactory.getLogger(FilesResource.class);

    private String filesDirectory;
    private String baseUrl;
    private File baseDirectory;
    private static final String DELIMITER = "1111111";
    private CustomerDAO customerDAO;
    private ApplicationDAO applicationDAO;
    private APKFileAnalyzer apkFileAnalyzer;
    private ConfigurationFileDAO configurationFileDAO;
    private IconDAO iconDAO;

    /**
     * <p>A constructor required by Swagger.</p>
     */
    public FilesResource() {
    }

    @Inject
    public FilesResource(@Named("files.directory") String filesDirectory,
                         @Named("base.url") String baseUrl,
                         CustomerDAO customerDAO,
                         ApplicationDAO applicationDAO,
                         APKFileAnalyzer apkFileAnalyzer,
                         ConfigurationFileDAO configurationFileDAO,
                         IconDAO iconDAO) {
        this.filesDirectory = filesDirectory;
        this.baseDirectory = new File(filesDirectory);
        this.customerDAO = customerDAO;
        this.applicationDAO = applicationDAO;
        this.configurationFileDAO = configurationFileDAO;
        this.iconDAO = iconDAO;
        if (!this.baseDirectory.exists()) {
            this.baseDirectory.mkdirs();
        }
        this.apkFileAnalyzer = apkFileAnalyzer;

        this.baseUrl = baseUrl;
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Get all files",
            notes = "Gets the list of all available files",
            response = HFile.class,
            responseContainer = "List"
    )
    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllFiles() {
        return Response.OK(this.generateFilesList((String)null));
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Remove a file",
            notes = "Removes the file from the MDM server"
    )
    @POST
    @Path("/remove")
    public Response removeFile(HFile file) {
        return SecurityContext.get().getCurrentUser().map(u -> {
            Customer customer = customerDAO.findById(u.getCustomerId());

            java.nio.file.Path filePath;
            if (customer.getFilesDir() == null || customer.getFilesDir().isEmpty()) {
                filePath = Paths.get( this.filesDirectory, file.getPath(), file.getName());
            } else {
                filePath = Paths.get(this.filesDirectory, customer.getFilesDir(), file.getPath(), file.getName());
            }

            // Check if file is not used
            final String fileName = file.getName();
            final String fileDirPath = file.getPath();
            if (this.configurationFileDAO.isFileUsed(fileName)) {
                return Response.FILE_USED();
            } else if (this.iconDAO.isFileUsed(fileName)) {
                return Response.FILE_USED();
            } else if (this.applicationDAO.isFileUsed(customer, fileDirPath, fileName)) {
                return Response.FILE_USED();
            }

            try {
                Files.delete(filePath);
                return Response.OK();
            } catch (IOException e) {
                e.printStackTrace();
                return Response.ERROR("error.file.deletion");
            }
        }).orElse(Response.PERMISSION_DENIED());
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Complete file upload",
            notes = "Commits the file upload to MDM server. Returns the uploaded file data",
            response = HFile.class
    )
    @POST
    @Path("/move")
    public Response moveFile(MoveFileRequest moveFileRequest) {
        return SecurityContext.get().getCurrentUser().map(u -> {
            Customer customer = customerDAO.findById(u.getCustomerId());
            try {
                File movedFile = FileUtil.moveFile(customer, filesDirectory, moveFileRequest.getLocalPath(), moveFileRequest.getPath());
                if (movedFile != null) {
                    List<HFile> result = new LinkedList<>();
                    handleFile(movedFile, result, null, customer);
                    return Response.OK(result.get(0));
                } else {
                    return Response.ERROR("error.file.save");
                }
            } catch (FileExistsException e) {
                return Response.FILE_EXISTS();
            }
        }).orElse(Response.PERMISSION_DENIED());
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Search files",
            notes = "Search files meeting the specified filter value",
            response = HFile.class,
            responseContainer = "List"
    )
    @GET
    @Path("/search/{value}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFilesByName(@PathParam("value") @ApiParam("A filter value") String value) {
        return Response.OK(this.generateFilesList(value));
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Get applications",
            notes = "Gets the list of applications using the file",
            response = Application.class,
            responseContainer = "List"
    )
    @GET
    @Path("/apps/{url}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getApplicationsForFile(@PathParam("url") @ApiParam("An URL referencing the file") String url) {
        try {
            String decodedUrl = URLDecoder.decode(url, "UTF-8");
            return Response.OK(this.applicationDAO.getAllApplicationsByUrl(decodedUrl));
        } catch (Exception e) {
            logger.error("Unexpected error when getting the list of applications by URL", e);
            return Response.INTERNAL_ERROR();
        }
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Upload file",
            notes = "Uploads the file to server. Returns a path to uploaded file",
            response = FileUploadResult.class
    )
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFiles(@FormDataParam("file") InputStream uploadedInputStream,
                                @ApiParam("A file to upload") @FormDataParam("file") FormDataContentDisposition fileDetail) throws Exception {
        try {
            // For some reason, the browser sends the file name in ISO_8859_1, so we use a workaround to convert
            // it to UTF_8 and enable non-ASCII characters
            // https://stackoverflow.com/questions/50582435/jersey-filename-encoded
            String fileName = new String(fileDetail.getFileName().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
            String adjustedFileName = fileName
                    .replace(' ', '_')
                    .replace('+', '_')          // Not valid in URL
                    .replace('%', '_')          // Not valid in URL
                    .replace("(", "")           // These characters are used by Windows when a file is downloaded twice
                    .replace(")", "");
            File uploadFile = File.createTempFile(adjustedFileName + DELIMITER, ".temp");
            writeToFile(uploadedInputStream, uploadFile.getAbsolutePath());

            FileUploadResult result = new FileUploadResult();
            result.setServerPath(uploadFile.getAbsolutePath());

            if (fileName.endsWith("apk")) {
                final APKFileDetails apkFileDetails;
                apkFileDetails = this.apkFileAnalyzer.analyzeFile(uploadFile.getAbsolutePath());
                result.setFileDetails(apkFileDetails);

                ApplicationVersion version;
                if (apkFileDetails.getVersionCode() != 0) {
                    version = this.applicationDAO.findApplicationVersionByCode(apkFileDetails.getPkg(), apkFileDetails.getVersionCode());
                    if (version != null && !version.getVersion().equals(apkFileDetails.getVersion())) {
                        // Version with the same version code but different version name exists
                        // This is a violation of Android versioning guidelines - disable upload
                        return Response.DUPLICATE_ENTITY("form.application.version.code.exists");
                    }
                }

                version = this.applicationDAO.findApplicationVersion(apkFileDetails.getPkg(), apkFileDetails.getVersion());
                if (StringUtil.isEmpty(apkFileDetails.getArch())){
                    if (version != null) {
                        result.setExists(true);
                    }
                } else if (apkFileDetails.getArch().equals(Application.ARCH_ARMEABI)) {
                    // If version for arm64 is already uploaded, set the complete flag
                    result.setComplete(version != null && !StringUtil.isEmpty(version.getUrlArm64()));
                    // Check if version is already uploaded
                    result.setExists(version != null && (!version.isSplit() || !StringUtil.isEmpty(version.getUrlArmeabi())));
                } else if (apkFileDetails.getArch().equals(Application.ARCH_ARM64)) {
                    result.setComplete(version != null && !StringUtil.isEmpty(version.getUrlArmeabi()));
                    result.setExists(version != null && (!version.isSplit() || !StringUtil.isEmpty(version.getUrlArm64())));
                }

                final List<Application> dbAppsByPkg = this.applicationDAO.findByPackageId(apkFileDetails.getPkg());
                if (!dbAppsByPkg.isEmpty()) {
                    final Application dbApp = dbAppsByPkg.get(0);
                    final Application dbAppCopy = new Application();
                    dbAppCopy.setId(dbApp.getId());
                    dbAppCopy.setShowIcon(dbApp.getShowIcon());
                    dbAppCopy.setUseKiosk(dbApp.getUseKiosk());
                    dbAppCopy.setRunAfterInstall(dbApp.isRunAfterInstall());
                    dbAppCopy.setRunAtBoot(dbApp.isRunAtBoot());
                    dbAppCopy.setSystem(dbApp.isSystem());
                    dbAppCopy.setName(dbApp.getName());

                    result.setApplication(dbAppCopy);
                }
            }


            return Response.OK(result);
        } catch (Exception e) {
            logger.error("Unexpected error when handling file upload", e);
            return Response.ERROR();
        }
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Download a file",
            notes = "Downloads the content of the file",
            responseHeaders = {@ResponseHeader(name = "Content-Disposition")}
    )
    @GET
    @Path("/{filePath}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public javax.ws.rs.core.Response downloadFile(@PathParam("filePath") @ApiParam("A path to a file") String filePath) throws Exception {
        File file = new File(filePath + "/" + URLDecoder.decode(filePath, "UTF8"));
        if (!file.exists()) {
            return javax.ws.rs.core.Response.status(404).build();
        } else {
            ContentDisposition contentDisposition = ContentDisposition.type("attachment").fileName(file.getName()).creationDate(new Date()).build();
            return javax.ws.rs.core.Response.ok( ( StreamingOutput ) output -> {
                try {
                    InputStream input = new FileInputStream( file );
                    IOUtils.copy(input, output);
                    output.flush();
                } catch ( Exception e ) { e.printStackTrace(); }
            } ).header( "Content-Disposition", contentDisposition ).build();

        }
    }

    private List<HFile> generateFilesList(String value) {
        List<HFile> files = SecurityContext.get().getCurrentUser().map(u -> {
            Customer customer = customerDAO.findById(u.getCustomerId());

            List<HFile> result = new LinkedList<>();
            this.handleFile(new File(this.baseDirectory, customer.getFilesDir()), result, value, customer);
            return result;

        }).orElse(new LinkedList<>());

        files.sort(null);

        return files;
    }

    private void handleFile(File file, List<HFile> result, String value, Customer customer) {
        final String customerFilesBaseDir = customer.getFilesDir();
        if (file != null && file.exists()) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                File[] filesArray = files;
                int length = files.length;

                for(int i = 0; i < length; ++i) {
                    File fl = filesArray[i];
                    this.handleFile(fl, result, value, customer);
                }
            } else if (value == null || file.getName().contains(value)) {

                String path = file.getParentFile().getAbsolutePath().replace(this.filesDirectory.replace("/", File.separator), "");
                if (!path.endsWith(File.separator)) {
                    path += File.separator;
                }
                String url;
                if (customerFilesBaseDir != null && !customerFilesBaseDir.isEmpty()) {
                    path = path.substring((File.separator + customerFilesBaseDir + File.separator).length()); // Strip off the name of directory for customer files
                    url = String.format("%s/files/%s/%s", this.baseUrl, customerFilesBaseDir, path.replace(File.separator, "/") + file.getName());
                } else {
                    url = String.format("%s/files%s", this.baseUrl, path.replace(File.separator, "/") + file.getName());
                }

                final HFile fileObj = new HFile(path, file.getName(), url);

                fileObj.setUsedByConfigurations(this.configurationFileDAO.getUsingConfigurations(customer.getId(), fileObj.getName()));
                fileObj.setUsedByIcons(this.iconDAO.getUsingIcons(customer.getId(), fileObj.getName()));
                fileObj.setUsedByApps(this.applicationDAO.getUsingApps(customer, fileObj.getPath(), fileObj.getName()));

                result.add(fileObj);
            }
        }

    }

}