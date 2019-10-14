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

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * <p>An enumeration over the resources which are targets for audit tracking.</p>
 *
 * @author isv
 */
public enum ResourceAuditInfo {
    LOGIN("/rest/public/auth/login", true, "plugin.audit.action.user.login"),
    JWT_LOGIN("/rest/public/jwt/login", true, "plugin.audit.action.api.login");

    /**
     * <p>An URI for the REST resource to track audit log for.</p>
     */
    private final String uri;

    /**
     * <p>A flag indicating if exact match for the resource URI is required in order to have the incoming request get
     * audited.</p>
     */
    private final boolean uriExactMatch;

    /**
     * <p>A key in message resource bundle referring to description of action mapped to audited request.</p>
     */
    private final String auditLogAction;

    /**
     * <p>Constructs new <code>ResourceAuditInfo</code> instance. This implementation does nothing.</p>
     */
    ResourceAuditInfo(String uri, boolean uriExactMatch, String auditLogAction) {
        this.uri = uri;
        this.uriExactMatch = uriExactMatch;
        this.auditLogAction = auditLogAction;
    }

    /**
     * <p>Gets the auditor for the specified request/response chain.</p>
     *
     * @param request an incoming request to be processed.
     * @param response a response to be sent to client.
     * @param chain a filter chain.
     * @return an auditor for the specified request/response chain.
     */
    public ResourceAuditor getResourceAuditor(ServletRequest request, ServletResponse response, FilterChain chain) {
        return new ResourceAuditor(auditLogAction, request, response, chain);
    }

    /**
     * <p>Checks if specified request URI matches this audit rule.</p>
     *
     * @param requestUri an URI for the current request being processed by application.
     * @return <code>true</code> if there is a match; <code>false</code> otherwise.
     */
    private boolean matches(String requestUri) {
        return this.uriExactMatch && this.uri.equals(requestUri);
    }

    /**
     * <p>Finds the details for audit process (if any) to be performed against specified request URI.</p>
     *
     * @param requestUri an URI for the current request being processed by application.
     * @return the details for audit process to apply to processed request.
     */
    public static Optional<ResourceAuditInfo> findAuditInfo(String requestUri) {
        return Stream.of(ResourceAuditInfo.values())
                .filter(info -> info.matches(requestUri))
                .findAny();
    }
}
