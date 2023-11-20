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

package com.hmdm.rest.filter;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.inject.Named;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Singleton
public class PublicIPFilter implements Filter {

    BaseIPFilter filter;

    public PublicIPFilter() {
    }

    @Inject
    public PublicIPFilter(@Named("device.allowed.address") String whitelist,
                          @Named("proxy.addresses") String proxyIps,
                          @Named("proxy.ip.header") String ipHeader) {
        if (!whitelist.equals("")) {
            filter = new BaseIPFilter(whitelist, proxyIps, ipHeader);
        }
    }

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void destroy() {
    }

    public boolean match(ServletRequest servletRequest) {
        if (filter != null) {
            return filter.match((HttpServletRequest)servletRequest);
        }
        return true;
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (filter != null && !filter.match((HttpServletRequest) servletRequest)) {
            ((HttpServletResponse)servletResponse).sendError(403);
            return;
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
