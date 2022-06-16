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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.inject.Named;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hmdm.notification.rest.NotificationResource;
import com.hmdm.persistence.ApplicationDAO;
import com.hmdm.rest.json.Response;
import com.hmdm.util.CryptoUtil;
import org.apache.poi.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DownloadFilesServlet extends HttpServlet {
    private final ApplicationDAO applicationDAO;
    private final String filesDirectory;
    private final File baseDirectory;

    private boolean secureEnrollment;
    private String hashSecret;

    private static final String HEADER_ENROLLMENT_SIGNATURE = "X-Request-Signature";

    private static final Logger log = LoggerFactory.getLogger(DownloadFilesServlet.class);

    @Inject
    public DownloadFilesServlet(ApplicationDAO applicationDAO,
                                @Named("files.directory") String filesDirectory,
                                @Named("secure.enrollment") boolean secureEnrollment,
                                @Named("hash.secret") String hashSecret) {
        this.applicationDAO = applicationDAO;
        this.filesDirectory = filesDirectory;
        this.baseDirectory = new File(filesDirectory);
        this.secureEnrollment = secureEnrollment;
        this.hashSecret = hashSecret;
        if (!this.baseDirectory.exists()) {
            this.baseDirectory.mkdirs();
        }

    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = URLDecoder.decode(req.getRequestURI(), "UTF8");
        int index = path.indexOf("/files/", 0) + "/files/".length();
        path = path.substring(index);

        if (secureEnrollment && !applicationDAO.isMainApp(req.getRequestURL().toString())) {
            String signature = req.getHeader(HEADER_ENROLLMENT_SIGNATURE);
            if (signature == null) {
                log.warn("No signature for file request " + path);
                resp.sendError(403);
                return;
            }
            try {
                String goodSignature = CryptoUtil.getSHA1String(hashSecret + path);
                if (!signature.equalsIgnoreCase(goodSignature)) {
                    log.warn("Wrong signature for file request " + path + ": " + signature + " Should be: " + goodSignature);
                    resp.sendError(403);
                    return;
                }
            } catch (Exception e) {
            }
        }

        File file = new File(String.format("%s/%s", this.filesDirectory, path));
        if (file.exists()) {

            String range = req.getHeader("Range");
            if (range != null && range.startsWith("bytes=")) {
                sendPartialContent(range.substring(6), file, resp);
                return;
            }

            try (InputStream input = new FileInputStream(file);
                 ServletOutputStream outputStream = resp.getOutputStream()) {
                long length = file.length();
                if (length <= 2147483647L) {
                    resp.setContentLength((int)length);
                } else {
                    resp.addHeader("Content-Length", Long.toString(length));
                }

                IOUtils.copy(input, outputStream);
                outputStream.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Not found: " + file.getAbsolutePath());
            resp.sendError(404);
        }

    }

    private void sendPartialContent(String rangeStr, File file, HttpServletResponse resp) {
        try {
            String[] range = rangeStr.split("-");
            Long start = Long.parseLong(range[0]);
            Long end = null;
            if (range.length > 1) {
                end = Long.parseLong(range[1]);
            }
            InputStream input = new FileInputStream(file);
            ServletOutputStream outputStream = resp.getOutputStream();
            long length = file.length();
            if (end == null) {
                end = length;
            }

            resp.setStatus(206);
            resp.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + length);
            long contentLength = end - start;
            if (length <= 2147483647L) {
                resp.setContentLength((int)contentLength);
            } else {
                resp.addHeader("Content-Length", Long.toString(contentLength));
            }

            input.skip(start);

            IOUtils.copy(input, outputStream, contentLength);
            outputStream.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
