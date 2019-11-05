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
import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Date;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import org.apache.poi.util.IOUtils;
import com.hmdm.persistence.domain.Video;
import com.hmdm.rest.json.Response;

import static com.hmdm.util.FileUtil.writeToFile;

@Singleton
@Path("/videos")
public class VideosResource {
    private String videoDirectory;
    private String baseUrl;

    public VideosResource() {
    }

    @Inject
    public VideosResource(@Named("video.directory") String videoDirectory, @Named("base.url") String baseUrl) {
        this.videoDirectory = videoDirectory;
        this.baseUrl = baseUrl;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadVideo(@FormDataParam("file") InputStream uploadedInputStream,
                                @FormDataParam("file") FormDataContentDisposition fileDetail) throws Exception {
        File videoDir = new File(this.videoDirectory);
        if (!videoDir.exists()) {
            videoDir.mkdirs();
        }

        File uploadFile = new File(videoDir.getAbsolutePath(), fileDetail.getFileName());
        writeToFile(uploadedInputStream, uploadFile.getAbsolutePath());
        Video video = new Video();
        video.setPath(String.format("%s/rest/public/videos/%s", this.baseUrl, URLEncoder.encode(fileDetail.getFileName(), "UTF8")));
        return Response.OK(video);
    }

    @GET
    @Path("/{fileName}")
    @Produces({"application/octet-stream"})
    public javax.ws.rs.core.Response downloadVideo(@PathParam("fileName") String fileName) throws Exception {
        File videoDir = new File(this.videoDirectory);
        if (!videoDir.exists()) {
            videoDir.mkdirs();
        }

        File videoFile = new File(videoDir, URLDecoder.decode(fileName, "UTF8"));
        if (!videoFile.exists()) {
            return javax.ws.rs.core.Response.status(404).build();
        } else {
            ContentDisposition contentDisposition = ContentDisposition.type("attachment").fileName(videoFile.getName()).creationDate(new Date()).build();
            return javax.ws.rs.core.Response.ok( ( StreamingOutput ) output -> {
                try {
                    InputStream input = new FileInputStream( videoFile );
                    IOUtils.copy(input, output);
                    output.flush();
                } catch ( Exception e ) { e.printStackTrace(); }
            } ).header( "Content-Disposition", contentDisposition ).build();

        }
    }
}
