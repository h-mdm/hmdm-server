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

package com.hmdm.plugins.push.persistence;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hmdm.persistence.AbstractDAO;
import com.hmdm.plugins.push.persistence.domain.PluginPushMessage;
import com.hmdm.plugins.push.persistence.mapper.PushMessageMapper;
import com.hmdm.plugins.push.rest.json.PushMessageFilter;
import com.hmdm.security.SecurityContext;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>A DAO for {@link PluginPushMessage} domain objects.</p>
 *
 * @author isv
 */
@Singleton
public class PushDAO extends AbstractDAO<PluginPushMessage> {

    private static final Logger logger = LoggerFactory.getLogger(PushDAO.class);

    /**
     * <p>An interface to persistence layer.</p>
     */
    private final PushMessageMapper pushMessageMapper;

    /**
     * <p>Constructs new <code>PushDAO</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public PushDAO(PushMessageMapper pushMessageMapper) {
        this.pushMessageMapper = pushMessageMapper;
    }


    /**
     * <p>Finds the message records matching the specified filter.</p>
     *
     * @param filter a filter used to narrowing down the search results.
     * @return a list of message records matching the specified filter.
     */
    @Transactional
    public List<PluginPushMessage> findAll(PushMessageFilter filter) {
        prepareFilter(filter);

        final List<PluginPushMessage> result = this.getListWithCurrentUser(currentUser -> {
            filter.setCustomerId(currentUser.getCustomerId());
            return this.pushMessageMapper.findAllMessages(filter);
        });

        return new ArrayList<>(result);
    }

    /**
     * <p>Counts the message records matching the specified filter.</p>
     *
     * @param filter a filter used to narrowing down the search results.
     * @return a number of message records matching the specified filter.
     */
    public long countAll(PushMessageFilter filter) {
        prepareFilter(filter);
        return SecurityContext.get().getCurrentUser()
                .map(user -> {
                    filter.setCustomerId(user.getCustomerId());
                    return this.pushMessageMapper.countAll(filter);
                })
                .orElse(0L);
    }

    /**
     * <p>Inserts the specified message into underlying persistent data store.</p>
     *
     * @param message message to insert
     */
    public void insertMessage(PluginPushMessage message) {
        SecurityContext.get().getCurrentUser().ifPresent(user -> {
            message.setCustomerId(user.getCustomerId());
            this.pushMessageMapper.insertMessage(message);
        });
    }

    /**
     * <p>Deletes the message</p>
     *
     * @param id message identifier
     */
    public void deleteMessage(int id) {
        this.pushMessageMapper.deleteMessage(id);
    }

    /**
     * <p>Purges old messages</p>
     *
     * @param days age of messages in days
     */
    public void purgeOldMessages(int days) {
        SecurityContext.get().getCurrentUser().ifPresent(user -> {
            long ts = 0;
            if (days > 0) {
                ts = System.currentTimeMillis() - days * 86400000L;
            }
            this.pushMessageMapper.purgeOldMessages(ts, user.getCustomerId());
        });
    }

    /**
     * <p>Prepares the filter for usage by mapper.</p>
     *
     * @param filter a filter provided by request.
     */
    private static void prepareFilter(PushMessageFilter filter) {
        if (filter.getDeviceFilter() != null) {
            if (filter.getDeviceFilter().trim().isEmpty()) {
                filter.setDeviceFilter(null);
            } else {
                filter.setDeviceFilter('%' + filter.getDeviceFilter().trim() + '%');
            }
        }
        if (filter.getMessageFilter() != null) {
            if (filter.getMessageFilter().trim().isEmpty()) {
                filter.setMessageFilter(null);
            } else {
                filter.setMessageFilter('%' + filter.getMessageFilter().trim() + '%');
            }
        }
    }

}
