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

import com.hmdm.notification.PushSenderPolling;
import com.hmdm.notification.persistence.NotificationDAO;
import com.hmdm.notification.persistence.domain.PushMessage;
import com.hmdm.notification.rest.json.PlainPushMessage;
import com.hmdm.persistence.UnsecureDAO;
import com.hmdm.persistence.domain.Device;
import com.hmdm.rest.filter.PublicIPFilter;
import com.hmdm.rest.json.Response;
import com.hmdm.util.CryptoUtil;
import com.hmdm.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * <p>A resource to be used for publishing/receiving the notification messages.</p>
 *
 * @author seva
 */
@Singleton
@WebServlet(urlPatterns = "/rest/notification/polling/*", asyncSupported = true)
public class LongPollingServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(LongPollingServlet.class);
    private UnsecureDAO unsecureDAO;
    private NotificationDAO notificationDAO;
    private PushSenderPolling pushSenderPolling;
    private String hashSecret;
    private boolean secureEnrollment;
    private PublicIPFilter publicIPFilter;
    private long pollingTimeout;
    private static final String HEADER_SIGNATURE = "X-Request-Signature";
    public static final String BASE_PATH = "/rest/notification/polling/";

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    /**
     * <p>A constructor required by Swagger.</p>
     */
    public LongPollingServlet() {
        log.error("Empty constructor called!");
    }

    /**
     * <p>Constructs new <code>NotificationResource</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public LongPollingServlet(UnsecureDAO unsecureDAO,
                              NotificationDAO notificationDAO,
                              PushSenderPolling pushSenderPolling,
                              PublicIPFilter publicIPFilter,
                              @Named("polling.timeout") long pollingTimeout,
                              @Named("secure.enrollment") boolean secureEnrollment,
                              @Named("hash.secret") String hashSecret) {
        this.unsecureDAO = unsecureDAO;
        this.notificationDAO = notificationDAO;
        this.pushSenderPolling = pushSenderPolling;
        this.publicIPFilter = publicIPFilter;
        this.hashSecret = hashSecret;
        this.pollingTimeout = pollingTimeout * 1000L;
        this.secureEnrollment = secureEnrollment;
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        String path = URLDecoder.decode(req.getRequestURI(), "UTF8");
        int index = path.indexOf(BASE_PATH, 0) + BASE_PATH.length();
        String deviceNumber = path.substring(index);

        if (!publicIPFilter.match(req)) {
            log.warn("Request blocked by IP: " + req.getRemoteAddr());
            resp.sendError(403);
            return;
        }
        if (secureEnrollment) {
            String signature = req.getHeader(HEADER_SIGNATURE);
            if (signature == null) {
                log.warn("No signature for file request " + path);
                resp.sendError(403);
                return;
            }
            try {
                String goodSignature = CryptoUtil.getSHA1String(hashSecret + path);
                if (!signature.equalsIgnoreCase(goodSignature)) {
                    log.warn("Wrong signature for push request from " + path + ": " + signature + " Should be: " + goodSignature);
                    resp.sendError(403);
                    return;
                }
            } catch (Exception e) {
            }
        }

        Device device = unsecureDAO.getDeviceByNumber(deviceNumber);
        if (device == null) {
            log.warn("No device with number: " + deviceNumber);
            resp.sendError(404);
            return;
        }

        // Unfortunately the output buffer can't be disabled or reduced (the minimal buffer size is 8192)
        // Even setting in server.xml: <Connector ... socket.appWriteBufSize="1" /> doesn't change anything!
        // Therefore, when the client is disconnected, the response is still "sent" to him without an exception.
        // This may cause message loss if a message is sent within a minute after the client's disconnection.
        // A workaround would be to use a padding so the response would exceed 8192 bytes, but this will increase
        // the traffic.
        //resp.setBufferSize(0);
        req.setAttribute("org.apache.catalina.ASYNC_SUPPORTED", true);
        final AsyncContext asyncContext = req.startAsync();
        asyncContext.setTimeout(pollingTimeout);
        asyncContext.addListener(new AsyncListener() {
            @Override
            public void onComplete(AsyncEvent event) throws IOException {
                log.debug("onComplete");
                List<PushMessage> messages = pushSenderPolling.getPendingMessages(event.getAsyncContext());
                if (messages.size() > 0) {
                    log.info("Delivering push-messages to device '{}': {}", deviceNumber, messages);
                }
                StringBuilder sb = new StringBuilder();
                sb.append("{\"status\":\"OK\",\"message\":null,\"data\":[");
                boolean firstEntry = true;
                for (PushMessage m : messages) {
                    if (firstEntry) {
                        firstEntry = false;
                    } else {
                        sb.append(",");
                    }
                    sb.append(m.toJsonString());
                }
                sb.append("]}");
                try {
                    log.debug("Buffer size: " + resp.getBufferSize());
                    resp.setStatus(200);
                    resp.setContentType("application/json");
                    resp.getOutputStream().write(sb.toString().getBytes("UTF-8"));
                    resp.getOutputStream().flush();
                    log.debug("Succesfully delivered");
                } catch (Exception e) {
                    log.warn("Failed to deliver push messages to device '{}': {}", deviceNumber, e.getMessage());
                    // Put pending messages back to the database
                    for (PushMessage m : messages) {
                        notificationDAO.send(m);
                    }
                }
                pushSenderPolling.unregister(event.getAsyncContext());
            }

            @Override
            public void onTimeout(AsyncEvent event) throws IOException {
                log.debug("onTimeout");
                // Timeout is followed by the Complete event, so nothing to do here
            }

            @Override
            public void onError(AsyncEvent event) throws IOException {
                log.debug("onError");
                pushSenderPolling.unregister(event.getAsyncContext());
            }

            @Override
            public void onStartAsync(AsyncEvent event) throws IOException {
                log.debug("onStartAsync");
            }
        });

        pushSenderPolling.register(device.getId(), asyncContext);
        List<PushMessage> offlineMessages = notificationDAO.getPendingMessagesForDelivery(device.getId());
        if (offlineMessages.size() > 0) {
            // This function completes the inquiry
            pushSenderPolling.sendPending(device.getId(), offlineMessages);
        }
    }
}
