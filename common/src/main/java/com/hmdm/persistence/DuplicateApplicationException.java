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

/**
 * <p>An exception to be thrown in case there is an attempt to create or update an application while another application
 * with same package ID and version already exists.</p>
 *
 * @author isv
 */
public class DuplicateApplicationException extends RuntimeException {

    /**
     * <p>Constructs new <code>DuplicateApplicationException</code> instance. This implementation does nothing.</p>
     */
    DuplicateApplicationException(String pkg, String version, Integer customerId) {
        super(String.format("Application %s v%s already exists. Customer account ID: %s", pkg, version, customerId));
    }
}
