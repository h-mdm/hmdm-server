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
import com.hmdm.plugins.push.persistence.domain.PluginPushSchedule;
import com.hmdm.plugins.push.persistence.mapper.PushMessageMapper;
import com.hmdm.plugins.push.persistence.mapper.PushScheduleMapper;
import com.hmdm.plugins.push.rest.json.PushMessageFilter;
import com.hmdm.plugins.push.rest.json.PushScheduleFilter;
import com.hmdm.security.SecurityContext;
import org.apache.ibatis.annotations.Param;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>A DAO for {@link PluginPushSchedule} domain objects.</p>
 *
 * @author seva
 */
@Singleton
public class PushScheduleDAO extends AbstractDAO<PluginPushSchedule> {

    private static final Logger logger = LoggerFactory.getLogger(PushScheduleDAO.class);

    /**
     * <p>An interface to persistence layer.</p>
     */
    private final PushScheduleMapper pushScheduleMapper;

    /**
     * <p>Constructs new <code>PushDAO</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public PushScheduleDAO(PushScheduleMapper pushScheduleMapper) {
        this.pushScheduleMapper = pushScheduleMapper;
    }


    /**
     * <p>Finds the scheduled messages matching the specified filter.</p>
     *
     * @param filter a filter used to narrowing down the search results.
     * @return a list of message records matching the specified filter.
     */
    @Transactional
    public List<PluginPushSchedule> findAll(PushScheduleFilter filter) {
        prepareFilter(filter);

        final List<PluginPushSchedule> result = this.getListWithCurrentUser(currentUser -> {
            filter.setCustomerId(currentUser.getCustomerId());
            return this.pushScheduleMapper.findAll(filter);
        });

        return new ArrayList<>(result);
    }

    /**
     * <p>Counts the message records matching the specified filter.</p>
     *
     * @param filter a filter used to narrowing down the search results.
     * @return a number of message records matching the specified filter.
     */
    public long countAll(PushScheduleFilter filter) {
        prepareFilter(filter);
        return SecurityContext.get().getCurrentUser()
                .map(user -> {
                    filter.setCustomerId(user.getCustomerId());
                    return this.pushScheduleMapper.countAll(filter);
                })
                .orElse(0L);
    }

    /**
     * <p>Inserts the specified scheduled message into underlying persistent data store.</p>
     *
     * @param message message to insert
     */
    public void insert(PluginPushSchedule message) throws Exception {
        if (!prepareAllMasks(message)) {
            throw new Exception("Invalid format");
        }
        SecurityContext.get().getCurrentUser().ifPresent(user -> {
            message.setCustomerId(user.getCustomerId());
            this.pushScheduleMapper.insert(message);
        });
    }

    /**
     * <p>Updates the specified scheduled message into underlying persistent data store.</p>
     *
     * @param message message to update
     */
    public void update(PluginPushSchedule message) throws Exception {
        if (!prepareAllMasks(message)) {
            throw new Exception("Invalid format");
        }
        SecurityContext.get().getCurrentUser().ifPresent(user -> {
            message.setCustomerId(user.getCustomerId());
            this.pushScheduleMapper.update(message);
        });
    }

    /**
     * <p>Deletes the message</p>
     *
     * @param id message identifier
     */
    public void delete(int id) {
        SecurityContext.get().getCurrentUser().ifPresent(user -> {
            this.pushScheduleMapper.delete(id, user.getCustomerId());
        });
    }

    public List<PluginPushSchedule> findMatchingTime() {
        Calendar c = Calendar.getInstance();
        return pushScheduleMapper.findMatchingTime(
                parseScheduleMask("" + c.get(Calendar.MINUTE), 60, false, "minute"),
                parseScheduleMask("" + c.get(Calendar.HOUR), 24, false, "hour"),
                parseScheduleMask("" + c.get(Calendar.DAY_OF_MONTH), 31, true, "day"),
                parseScheduleMask("" + c.get(Calendar.DAY_OF_WEEK), 7, true, "weekday"),
                parseScheduleMask("" + c.get(Calendar.MONTH), 12, true, "month")
        );
    }

    /**
     * <p>Prepares the filter for usage by mapper.</p>
     *
     * @param filter a filter provided by request.
     */
    private static void prepareFilter(PushScheduleFilter filter) {
        if (filter.getMessageFilter() != null) {
            if (filter.getMessageFilter().trim().isEmpty()) {
                filter.setMessageFilter(null);
            } else {
                filter.setMessageFilter('%' + filter.getMessageFilter().trim() + '%');
            }
        }
    }

    private static boolean prepareAllMasks(PluginPushSchedule message) {
        String minBit = parseScheduleMask(message.getMin(), 60, false, "minute");
        if (minBit == null) {
            return false;
        }
        message.setMinBit(minBit);

        String hourBit = parseScheduleMask(message.getHour(), 24, false, "hour");
        if (hourBit == null) {
            return false;
        }
        message.setHourBit(hourBit);

        String dayBit = parseScheduleMask(message.getDay(), 31, true, "day");
        if (dayBit == null) {
            return false;
        }
        message.setDayBit(dayBit);

        String weekdayBit = parseScheduleMask(message.getWeekday(), 7, true, "weekday");
        if (weekdayBit == null) {
            return false;
        }
        message.setWeekdayBit(weekdayBit);

        String monthBit = parseScheduleMask(message.getMonth(), 12, true, "month");
        if (monthBit == null) {
            return false;
        }
        message.setMonthBit(monthBit);

        return true;
    }

    private static String parseScheduleMask(String rawMask, int length, boolean startFromOne, String scope) {
        String mask = rawMask.replaceAll("\\s+","");
        StringBuilder res = new StringBuilder(length);
        if (mask.equals("*")) {
            for (int i = 0; i < length; i++) {
                res.append("1");
            }
        } else if (mask.startsWith("*/")) {
            String ns = mask.substring(2);
            int n;
            try {
                 n = Integer.parseInt(ns);
            } catch (NumberFormatException e) {
                logger.error("Failed to parse schedule mask for " + scope + ": " + mask);
                return null;
            }
            if (n < 1) {
                logger.error("Failed to parse schedule mask for " + scope + ": " + mask);
                return null;
            }
            for (int i = 0; i < length; i++) {
                res.append(i % n == 0 ? "1" : "0");
            }
        } else {
            int n;
            try {
                n = Integer.parseInt(mask);
            } catch (NumberFormatException e) {
                logger.error("Failed to parse schedule mask for " + scope + ": " + mask);
                return null;
            }
            if (startFromOne) {
                n--;
            }
            if (n < 0 || n >= length) {
                logger.error("Failed to parse schedule mask for " + scope + ": " + mask);
                return null;
            }
            for (int i = 0; i < length; i++) {
                res.append(i == n ? "1" : "0");
            }
        }
        return res.toString();
    }

}
