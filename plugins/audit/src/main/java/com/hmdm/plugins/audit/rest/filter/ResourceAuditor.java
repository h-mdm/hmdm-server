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

package com.hmdm.plugins.audit.rest.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmdm.persistence.domain.User;
import com.hmdm.plugins.audit.persistence.domain.AuditLogRecord;
import com.hmdm.plugins.audit.rest.AuditResource;
import com.hmdm.rest.json.Response;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Enumeration;

/**
 * <p>An auditor for a single request-response chain.</p>
 *
 * @author isv
 */
class ResourceAuditor {

    private static final Logger logger = LoggerFactory.getLogger(ResourceAuditor.class);

    /**
     * <p>A name of session attribute holding the details for current user.</p>
     */
    private static final String sessionCredentials = "credentials";

    /**
     * <p>A key in message resource bundle referring to description of action mapped to audited request.</p>
     */
    private final String auditLogActionKey;

    /**
     * <p>An incoming request being processed.</p>
     */
    private final ServletRequest request;

    /**
     * <p>A response to be sent to client.</p>
     */
    private final ServletResponseAuditWrapper response;

    /**
     * <p>A filter chain</p>
     */
    private final FilterChain chain;

    /**
     * <p>A flag indicating if request data must be saved as a payload.</p>
     */
    private final boolean payload;

    /**
     * <p>List of reverse proxy IPs</p>
     */
    private final String[] proxies;

    /**
     * <p>Name of the HTTP header containing the user IP address</p>
     */
    private final String ipHeader;

    /**
     * <p>Constructs new <code>ResourceAuditor</code> instance. This implementation does nothing.</p>
     */
    ResourceAuditor(String auditLogActionKey, ServletRequest request, ServletResponse response,
                    FilterChain chain, boolean payload, String proxyIps, String ipHeader) throws IOException {
        this.auditLogActionKey = auditLogActionKey;
        if (payload) {
            // Wrap request only if we need to log it
            this.request = new ServletRequestAuditWrapper((HttpServletRequest)request);
        } else {
            this.request = request;
        }
        this.response = new ServletResponseAuditWrapper((HttpServletResponse)response);
        this.chain = chain;
        this.payload = payload;
        if (!"".equals(proxyIps)) {
            proxies = proxyIps.split(",");
        } else {
            proxies = new String[0];
        }
        this.ipHeader = ipHeader;
    }

    /**
     * <p>Executes the request/response chain and captures data necessary for auditing.</p>
     *
     * @throws IOException if an unexpected I/O error occurs.
     * @throws ServletException if an unexpected error occurs.
     */
    void doProcess() throws IOException, ServletException {
        chain.doFilter(this.request, this.response);
    }

    /**
     * <p>Gets the audit log record for the target request/response chain.</p>
     *
     * @return an audit log record evaluated based on the request processing results or <code>null</code> if no such
     *         record is available.
     * @throws IOException if an unexpected I/O error occurs.
     */
    AuditLogRecord getAuditLogRecord() throws IOException {
        final HttpServletRequest httpRequest = (HttpServletRequest) this.request;
        final HttpSession session = httpRequest.getSession(false);
        User currentUser = null;
        if (session != null) {
            currentUser = (User) session.getAttribute(sessionCredentials);
        }

        String action = this.auditLogActionKey;
        String payloadString = null;
        String requestedLogin = null;
        if (payload) {
            ServletRequestAuditWrapper requestWrapper = (ServletRequestAuditWrapper)this.request;
            payloadString = "Method: " + requestWrapper.getMethod() + "\n" +
                    "URI: " + requestWrapper.getRequestURI();
            String body = requestWrapper.getBody();
            if (body != null && body.length() > 0) {
                if (needStripPassword(action)) {
                    // Since we parse JSON anyway, let's extract and log the login from the request
                    StripPasswordResponse response = stripPassword(body);
                    body = response.result;
                    requestedLogin = response.login;
                }
                payloadString += "\nBody: " + body;
            }
            payloadString += "\nUser-Agent: " + httpRequest.getHeader("User-Agent");
        }

        AuditLogRecord logRecord = new AuditLogRecord();
        logRecord.setCreateTime(System.currentTimeMillis());
        logRecord.setIpAddress(getRemoteAddr(httpRequest));
        if (currentUser != null) {
            logRecord.setCustomerId(currentUser.getCustomerId());
            logRecord.setLogin(currentUser.getLogin());
            logRecord.setUserId(currentUser.getId());
        } else {
            // 1 is the default customer ID, otherwise the record won't be visible
            logRecord.setCustomerId(1);
            if (requestedLogin != null) {
                logRecord.setLogin(requestedLogin);
            }
        }
        if (this.response.getStatus() == 200) {
            final byte[] content = this.response.getContent();
            ObjectMapper objectMapper = new ObjectMapper();
            final Response response = objectMapper.readValue(content, Response.class);
            if (response == null || response.getStatus() != Response.ResponseStatus.OK) {
                logRecord.setErrorCode(1);
            } else {
                logRecord.setErrorCode(0);
            }
        } else {
            logRecord.setErrorCode(2);
        }
        if (payload) {
            logRecord.setPayload(payloadString);
        }
        logRecord.setAction(action);

        return logRecord;
    }

    private String getRemoteAddr(HttpServletRequest request) {
        boolean isFromProxy = false;
//        String localAddr = request.getLocalAddr();
//        logger.info("Local address: " + localAddr);
        if (request.getRemoteAddr().equals(request.getLocalAddr())) {
            isFromProxy = true;
        } else {
            for (String p : proxies) {
                if (request.getRemoteAddr().equals(p.trim())) {
                    isFromProxy = true;
                    break;
                }
            }
        }
        if (isFromProxy) {
//            logger.info("From proxy: true");
//            Enumeration<String> headerNames = request.getHeaderNames();
//            if (headerNames != null) {
//                while (headerNames.hasMoreElements()) {
//                    String headerName = headerNames.nextElement();
//                    String headerValue = request.getHeader(headerName);
//                    logger.info(headerName + ": " + headerValue);
//                }
//            }

            String forwardedIp = request.getHeader(ipHeader);
            if (forwardedIp != null) {
                return forwardedIp;
            }
        }
        return request.getRemoteAddr();
    }

    private boolean needStripPassword(String action) {
        return "plugin.audit.action.user.login".equals(action) ||
               "plugin.audit.action.api.login".equals(action) ||
               "plugin.audit.action.password.changed".equals(action) ||
               "plugin.audit.action.update.user".equals(action);
    }

    private class StripPasswordResponse {
        String result;
        String login;
    }

    private StripPasswordResponse stripPassword(String jsonPayload) {
        StripPasswordResponse response = new StripPasswordResponse();
        response.result = jsonPayload;
        try {
            boolean dirty = false;
            JSONObject jsonObject = new JSONObject(jsonPayload);
            if (jsonObject.has("newPassword")) {
                jsonObject.put("newPassword", "******");
                dirty = true;
            }
            if (jsonObject.has("oldPassword")) {
                jsonObject.put("oldPassword", "******");
                dirty = true;
            }
            if (jsonObject.has("password")) {
                jsonObject.put("password", "******");
                dirty = true;
            }
            if (jsonObject.has("confirm")) {
                jsonObject.put("confirm", "******");
                dirty = true;
            }
            if (dirty) {
                response.result = jsonObject.toString();
                if (jsonObject.has("login")) {
                    response.login = jsonObject.getString("login");
                }
            }
        } catch (JSONException e) {
            // Do not change anything if not a valid JSON
        }
        return response;
    }
}
