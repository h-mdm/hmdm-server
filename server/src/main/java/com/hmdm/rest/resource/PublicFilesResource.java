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
import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.apache.poi.util.IOUtils;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLDecoder;
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

    @Inject
    public PublicFilesResource() {
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
    public javax.ws.rs.core.Response downloadFile(@PathParam("filePath") String filePath) throws Exception {
        // TODO : ISV : Needs to identify the device and do a security check if device is granted access to specified
        //  file
        File file = new File(filePath + "/" + URLDecoder.decode(filePath, "UTF8"));
        if (!file.exists()) {
            return javax.ws.rs.core.Response.status(404).build();
        } else {
            ContentDisposition contentDisposition = ContentDisposition.type("attachment").fileName(file.getName()).creationDate(new Date()).build();
            return javax.ws.rs.core.Response.ok( (StreamingOutput) output -> {
                try {
                    InputStream input = new FileInputStream( file );
                    IOUtils.copy(input, output);
                    output.flush();
                } catch ( Exception e ) { e.printStackTrace(); }
            } ).header( "Content-Disposition", contentDisposition ).build();

        }
    }
}