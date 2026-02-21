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

package com.hmdm.persistence;

import com.hmdm.security.SecurityException;

/**
 * <p>An exception to be raised when there is an attempt to edit/delete data related to common application by unauthorized
 * client.</p>
 *
 * @author isv
 */
public class CommonAppAccessException extends SecurityException {

    /**
     * <p>Constructs new <code>CommonAppAccessException</code> instance. This implementation does nothing.</p>
     */
    public CommonAppAccessException(String pkg, Integer customerId) {
        super(
                String.format(
                        "An attempt to common application %s is prohibited. Customer account ID: %s", pkg, customerId),
                403);
    }
}
