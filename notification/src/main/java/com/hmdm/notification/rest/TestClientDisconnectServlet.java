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

package com.hmdm.notification.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <p>Tomcat 9 bug evidence (?)</p>
 *
 * @author seva
 */
@Singleton
public class TestClientDisconnectServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(TestClientDisconnectServlet.class);

    public TestClientDisconnectServlet() {
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        log.info("Start generating the response");
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("Finish generating the response");
        try {
            resp.setBufferSize(0);
            // Here we always get 8192, so when the client is disconnected, the error is not raised...
            // Even if I set in server.xml: <Connector ... socket.appWriteBufSize="1" /> it doesn't change anything!
            log.info("Buffer size=" + resp.getBufferSize());
            resp.setBufferSize(1);
            log.info("Buffer size=" + resp.getBufferSize());
            resp.setStatus(200);
            resp.setContentType("text/html");
            // If the line exceeds 8192 bytes, we get the exception
            String response = "<html><body>This is a test servlet.</body></html>";
            resp.getOutputStream().write(response.getBytes());
            resp.getOutputStream().flush();
            resp.getOutputStream().close();
            log.info("Succesfully delivered");
        } catch (Exception e) {
            log.info("Failed to deliver the request: {}", e.getMessage());
        }
    }
}
