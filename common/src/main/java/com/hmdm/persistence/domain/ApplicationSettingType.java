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

package com.hmdm.persistence.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * <p>An enumeration over the application setting types.</p>
 */
public enum ApplicationSettingType {
    STRING(1),
    INTEGER(2),
    BOOLEAN(3);

    /**
     * <p>A map used for faster lookup of enum items by ID.</p>
     */
    private static Map<Integer, ApplicationSettingType> lookupMap = new HashMap<>();

    /*
     * <p>Build the lookup index.</p>
     */
    static {
        for (ApplicationSettingType item : values()) {
            lookupMap.put(item.getId(), item);
        }
    }

    // An ID of a type
    private int id;

    ApplicationSettingType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }


    /**
     * <p>Gets the app setting type mapped to specified ID.</p>
     *
     * @param id an ID of requested setting type.
     * @return an optional reference to setting type mapped to specified ID.
     */
    public static Optional<ApplicationSettingType> byId(int id) {
        return Optional.ofNullable(lookupMap.get(id));
    }
}
