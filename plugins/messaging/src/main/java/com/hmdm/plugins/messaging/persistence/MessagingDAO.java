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

package com.hmdm.plugins.messaging.persistence;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hmdm.persistence.AbstractDAO;
import com.hmdm.plugins.messaging.persistence.domain.Message;
import com.hmdm.plugins.messaging.persistence.mapper.MessageMapper;
import com.hmdm.plugins.messaging.rest.json.MessageFilter;
import com.hmdm.security.SecurityContext;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>A DAO for {@link Message} domain objects.</p>
 *
 * @author isv
 */
@Singleton
public class MessagingDAO extends AbstractDAO<Message> {

    private static final Logger logger = LoggerFactory.getLogger(MessagingDAO.class);

    /**
     * <p>An interface to persistence layer.</p>
     */
    private final MessageMapper messageMapper;

    /**
     * <p>Constructs new <code>MessagingDAO</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public MessagingDAO(MessageMapper messageMapper) {
        this.messageMapper = messageMapper;
    }


    /**
     * <p>Finds the message records matching the specified filter.</p>
     *
     * @param filter a filter used to narrowing down the search results.
     * @return a list of message records matching the specified filter.
     */
    @Transactional
    public List<Message> findAll(MessageFilter filter) {
        prepareFilter(filter);

        final List<Message> result = this.getListWithCurrentUser(currentUser -> {
            filter.setCustomerId(currentUser.getCustomerId());
            return this.messageMapper.findAllMessages(filter);
        });

        return new ArrayList<>(result);
    }

    /**
     * <p>Counts the message records matching the specified filter.</p>
     *
     * @param filter a filter used to narrowing down the search results.
     * @return a number of message records matching the specified filter.
     */
    public long countAll(MessageFilter filter) {
        prepareFilter(filter);
        return SecurityContext.get().getCurrentUser()
                .map(user -> {
                    filter.setCustomerId(user.getCustomerId());
                    return this.messageMapper.countAll(filter);
                })
                .orElse(0L);
    }

    /**
     * <p>Inserts the specified message into underlying persistent data store.</p>
     *
     * @param message message to insert
     */
    public void insertMessage(Message message) {
        SecurityContext.get().getCurrentUser().ifPresent(user -> {
            message.setCustomerId(user.getCustomerId());
            this.messageMapper.insertMessage(message);
        });
    }

    /**
     * <p>Updates the message status (sets as read)</p>
     *
     * @param id message identifier
     * @param status new status
     */
    public void updateMessageStatus(int id, int status) {
        this.messageMapper.updateMessageStatus(id, status);
    }

    /**
     * <p>Deletes the message</p>
     *
     * @param id message identifier
     */
    public void deleteMessage(int id) {
        this.messageMapper.deleteMessage(id);
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
            this.messageMapper.purgeOldMessages(ts, user.getCustomerId());
        });
    }

    /**
     * <p>Prepares the filter for usage by mapper.</p>
     *
     * @param filter a filter provided by request.
     */
    private static void prepareFilter(MessageFilter filter) {
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
