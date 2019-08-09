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

package com.hmdm.plugins.devicelog.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * <p>An enumeration over supported log levels.</p>
 *
 * @author isv
 */
public enum LogLevel {
    NONE(0),
    ERROR(1),
    WARNING(2),
    INFO(3),
    DEBUG(4),
    VERBOSE(5);


    /**
     * <p>A map used for faster lookup of enum items by ID.</p>
     */
    private static Map<Integer, LogLevel> lookupMap = new HashMap<>();

    /*
     * <p>Build the lookup index.</p>
     */
    static {
        for (LogLevel item : values()) {
            lookupMap.put(item.getId(), item);
        }
    }

    /**
     * <p>An identifier for the log level.</p>
     */
    private final int id;

    /**
     * <p>Constructs new <code>LogLevel</code> instance. This implementation does nothing.</p>
     */
    LogLevel(int id) {
        this.id = id;
    }

    /**
     * <p>Gets the ID for this log level.</p>
     *
     * @return an ID of this log level.
     */
    public int getId() {
        return id;
    }


    /**
     * <p>Gets the log level mapped to specified ID.</p>
     *
     * @param id an ID of requested log level.
     * @return an optional reference to log level mapped to specified ID.
     */
    public static Optional<LogLevel> byId(int id) {
        return Optional.ofNullable(lookupMap.get(id));
    }
}
