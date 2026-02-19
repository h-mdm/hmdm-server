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

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.apache.poi.util.IOUtils;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.StreamingOutput;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * <p>A REST API for accessing the files from mobile applications.</p>
 *
 * <p>This class must never be used. {@link DownloadFilesServlet} is used instead for downloading files from server
 * by mobile applications.</p>
 *
 * @author isv
 */
@Singleton
@Path("/public/files")
@Deprecated
public class PublicFilesResource {

    private String filesDirectory;

    @Inject
    public PublicFilesResource(@Named("files.directory") String filesDirectory) {
        this.filesDirectory = filesDirectory;
    }

    /**
     * <p>Sends content of the file to client.</p>
     *
     * @param filePath a relative path to a file.
     * @return a response to client.
     * @throws Exception if an unexpected error occurs.
     */
    @GET
    @Path("/{filePath}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public jakarta.ws.rs.core.Response downloadFile(@PathParam("filePath") String filePath) throws Exception {
        // TODO : ISV : Needs to identify the device and do a security check if device is granted access to specified
        //  file
        File file = new File(filesDirectory + "/" + URLDecoder.decode(filePath, StandardCharsets.UTF_8));
        if (!file.exists()) {
            return jakarta.ws.rs.core.Response.status(404).build();
        } else {
            ContentDisposition contentDisposition = ContentDisposition.type("attachment").fileName(file.getName()).creationDate(new Date()).build();
            return jakarta.ws.rs.core.Response.ok( (StreamingOutput) output -> {
                try {
                    InputStream input = new FileInputStream( file );
                    IOUtils.copy(input, output);
                    output.flush();
                } catch ( Exception e ) { e.printStackTrace(); }
            } ).header( "Content-Disposition", contentDisposition ).build();

        }
    }
}