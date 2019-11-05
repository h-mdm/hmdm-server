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

package com.hmdm.plugin.rest;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hmdm.plugin.service.PluginStatusCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <p>$</p>
 *
 * @author isv
 */
@Singleton
public class PluginAccessFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(PluginAccessFilter.class);

    private final PluginStatusCache pluginStatusCache;

    /**
     * <p>Constructs new <code>PluginAccessFilter</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public PluginAccessFilter(PluginStatusCache pluginStatusCache) {
        this.pluginStatusCache = pluginStatusCache;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            final String pathInfo = httpRequest.getServletPath();
            if (pathInfo != null && pathInfo.startsWith("/rest/plugins/")) {
                final String[] parts = pathInfo.split("/");
                if (parts.length > 3) {
                    final String pluginId = parts[3];
                    if (this.pluginStatusCache.isPluginDisabled(pluginId)) {
                        logger.info("Prohibiting access to disabled plugin '{}': {}", pluginId, pathInfo);
                        HttpServletResponse httpResponse = (HttpServletResponse) response;
                        httpResponse.sendError(404, "Plugin unavailable");
                        return;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Unexpected error when checking plugin for availability. The request goes on further down the chain.", e);
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }
}
