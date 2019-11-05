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
import com.hmdm.rest.json.Response;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * <p>An auditor for a single request-response chain.</p>
 *
 * @author isv
 */
class ResourceAuditor {

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
     * <p>Constructs new <code>ResourceAuditor</code> instance. This implementation does nothing.</p>
     */
    ResourceAuditor(String auditLogActionKey, ServletRequest request, ServletResponse response, FilterChain chain) {
        this.auditLogActionKey = auditLogActionKey;
        this.request = request;
        this.response = new ServletResponseAuditWrapper((HttpServletResponse) response);
        this.chain = chain;
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
        final HttpSession session = ((HttpServletRequest) this.request).getSession(false);
        User currentUser = null;
        if (session != null) {
            currentUser = (User) session.getAttribute(sessionCredentials);
        }

        if (this.response.getStatus() == 200) {
            final byte[] content = this.response.getContent();
            ObjectMapper objectMapper = new ObjectMapper();
            final Response response = objectMapper.readValue(content, Response.class);
            if (response != null) {
                if (response.getStatus() == Response.ResponseStatus.OK) {
                    AuditLogRecord logRecord = new AuditLogRecord();
                    logRecord.setAction(this.auditLogActionKey);
                    logRecord.setCreateTime(System.currentTimeMillis());
                    logRecord.setIpAddress(request.getRemoteAddr());
                    if (currentUser != null) {
                        logRecord.setCustomerId(currentUser.getCustomerId());
                        logRecord.setLogin(currentUser.getLogin());
                        logRecord.setUserId(currentUser.getId());
                    }

                    return logRecord;
                }
            }
        }

        return null;
    }
}
