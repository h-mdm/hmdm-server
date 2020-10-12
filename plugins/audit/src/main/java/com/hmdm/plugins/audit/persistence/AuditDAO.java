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

package com.hmdm.plugins.audit.persistence;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hmdm.persistence.AbstractDAO;
import com.hmdm.plugins.audit.persistence.domain.AuditLogRecord;
import com.hmdm.plugins.audit.persistence.mapper.AuditMapper;
import com.hmdm.plugins.audit.rest.json.AuditLogFilter;
import com.hmdm.security.SecurityContext;
import org.mybatis.guice.transactional.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>A DAO for {@link AuditLogRecord} domain objects.</p>
 *
 * @author isv
 */
@Singleton
public class AuditDAO extends AbstractDAO<AuditLogRecord> {

    /**
     * <p>An ORM mapper used for managing the audit log records data the database.</p>
     */
    private final AuditMapper mapper;

    /**
     * <p>Constructs new <code>AuditDAO</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public AuditDAO(AuditMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * <p>Inserts an audit log record into database.</p>
     *
     * @param logRecord a log record to be inserted.
     */
    public void insertAuditLogRecord(AuditLogRecord logRecord) {
        this.mapper.insertAuditLogRecord(logRecord);
    }

    /**
     * <p>Finds the audit log records matching the specified filter.</p>
     *
     * @param filter a filter used to narrowing down the search results.
     * @return a list of audit log records matching the specified filter.
     */
    @Transactional
    public List<AuditLogRecord> findAll(AuditLogFilter filter) {
        prepareFilter(filter);

        final List<AuditLogRecord> result = this.getListWithCurrentUser(currentUser -> {
            filter.setCustomerId(currentUser.getCustomerId());
            filter.setUserId(currentUser.getId());
            return this.mapper.findAllLogRecordsByCustomerId(filter);
        });

        return new ArrayList<>(result);
    }

    /**
     * <p>Counts the audit log records matching the specified filter.</p>
     *
     * @param filter a filter used to narrowing down the search results.
     * @return a number of audit log records matching the specified filter.
     */
    public long countAll(AuditLogFilter filter) {
        prepareFilter(filter);
        return SecurityContext.get().getCurrentUser()
                .map(user -> {
                    filter.setCustomerId(user.getCustomerId());
                    return this.mapper.countAll(filter);
                })
                .orElse(0L);
    }

    /**
     * <p>Prepares the filter for usage by mapper.</p>
     *
     * @param filter a filter provided by request.
     */
    private static void prepareFilter(AuditLogFilter filter) {
        if (filter.getMessageFilter() != null) {
            if (filter.getMessageFilter().trim().isEmpty()) {
                filter.setMessageFilter(null);
            } else {
                filter.setMessageFilter('%' + filter.getMessageFilter().trim() + '%');
            }
        }
        if (filter.getUserFilter() != null) {
            if (filter.getUserFilter().trim().isEmpty()) {
                filter.setUserFilter(null);
            } else {
                filter.setUserFilter(filter.getUserFilter().trim());
            }
        }
    }
}
