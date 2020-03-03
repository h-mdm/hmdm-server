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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hmdm.plugin.service.PluginStatusCache;
import com.hmdm.plugins.audit.AuditPluginConfigurationImpl;
import com.hmdm.plugins.audit.persistence.AuditDAO;
import com.hmdm.plugins.audit.persistence.domain.AuditLogRecord;
import com.hmdm.util.BackgroundTaskRunnerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Optional;

/**
 * <p>Intercepts incoming requests and logs the audit records for users activities.</p>
 *
 * @author isv
 */
@Singleton
public class AuditFilter implements Filter {

    /**
     * <p>A logger to be used for logging the audit log records.</p>
     */
    private static final Logger auditLogger = LoggerFactory.getLogger("AuditLogger");

    /**
     * <p>A logger used for logging other events encountered while filtering the requests.</p>
     */
    private static final Logger logger = LoggerFactory.getLogger(AuditFilter.class);

    /**
     * <p>A DAO to be used for inserting audit log records into database.</p>
     */
    private final AuditDAO auditDAO;

    /**
     * <p>A runner for the audit log record insertion tasks.</p>
     */
    private final BackgroundTaskRunnerService backgroundTaskRunnerService;

    /**
     * <p>The current status of installed plugins.</p>
     */
    private PluginStatusCache pluginStatusCache;

    /**
     * <p>Constructs new <code>AuditFilter</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public AuditFilter(AuditDAO auditDAO, BackgroundTaskRunnerService backgroundTaskRunnerService,
                       PluginStatusCache pluginStatusCache) {
        this.auditDAO = auditDAO;
        this.backgroundTaskRunnerService = backgroundTaskRunnerService;
        this.pluginStatusCache = pluginStatusCache;
    }

    /**
     * <p>Does nothing.</p>
     */
    @Override
    public void init(FilterConfig filterConfig) {
    }

    /**
     * <p>Intercepts the specified request/response chain and records the audit data if auditing for specified request
     * is supported.</p>
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        final String requestURI = httpRequest.getRequestURI();
        final String context = httpRequest.getContextPath();

        if (pluginStatusCache.isPluginDisabled(AuditPluginConfigurationImpl.PLUGIN_ID) ||
            httpRequest.getMethod().equalsIgnoreCase("GET")) {
            // GET requests are not recorded
            chain.doFilter(request, response);
            return;
        }

        final Optional<ResourceAuditInfo> auditInfo = ResourceAuditInfo.findAuditInfo(httpRequest.getMethod(), requestURI.substring(context.length()));
        boolean needAudit = auditInfo.isPresent();

        ResourceAuditor resourceAuditor = null;
        try {
            if (needAudit) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Will audit request {}", requestURI.substring(context.length()));
                }

                resourceAuditor = auditInfo.get().getResourceAuditor(request, response, chain);
                resourceAuditor.doProcess();
            } else {
                chain.doFilter(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (needAudit && resourceAuditor != null) {
                AuditLogRecord logRecord = resourceAuditor.getAuditLogRecord();
                if (logRecord != null) {
                    auditLogger.info(logRecord.toLogString());
                    this.backgroundTaskRunnerService.submitTask(new Task(logRecord));
                }

            }
        }
    }

    /**
     * <p>Does nothing.</p>
     */
    @Override
    public void destroy() {

    }

    /**
     * <p>A task to be used for inserting the audit log record into database in the background.</p>
     */
    private class Task implements Runnable {

        /**
         * <p>An audit log record to be inserted.</p>
         */
        private final AuditLogRecord logRecord;

        private Task(AuditLogRecord logRecord) {
            this.logRecord = logRecord;
        }

        /**
         * <pInserts the audit log record into database.</p>
         */
        @Override
        public void run() {
            try {
                auditDAO.insertAuditLogRecord(logRecord);
            } catch (Exception e) {
                logger.error("Unexpected error when inserting audit log record into database", e);
            }
        }
    }
}
