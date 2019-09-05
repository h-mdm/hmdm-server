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

package com.hmdm.util;

/**
 * <p>An exception to be thrown in case the APK-file could not be analyzed successfully.</p>
 *
 * @author isv
 */
public class APKFileAnalyzerException extends RuntimeException {

    /**
     * <p>Constructs new <code>APKFileAnalyzerException</code> instance. This implementation does nothing.</p>
     */
    public APKFileAnalyzerException(String message) {
        super(message);
    }

    /**
     * <p>Constructs new <code>APKFileAnalyzerException</code> instance. This implementation does nothing.</p>
     */
    public APKFileAnalyzerException(String message, Throwable cause) {
        super(message, cause);
    }

}
