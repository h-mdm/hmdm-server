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
 * <p>An exception to be thrown in case the package ID provided by uploaded APK-file for new application version does
 * not match the package ID for respective application.</p>
 *
 * @author isv
 */
public class ApplicationVersionPackageMismatchException extends RuntimeException {

    /**
     * <p>An actual package ID provided by uploaded APK-file.</p>
     */
    private final String actualPackageName;

    /**
     * <p>An expected package ID as set for respective application.</p>
     */
    private final String expectedPackageName;

    /**
     * <p>Constructs new <code>ApplicationVersionPackageMismatchException</code> instance. This implementation does nothing.</p>
     */
    public ApplicationVersionPackageMismatchException(String actualPackageName, String expectedPackageName) {
        super(String.format("Application version package from uploaded file does not match the application package. Expected %s but got %s",
                expectedPackageName, actualPackageName));
        this.actualPackageName = actualPackageName;
        this.expectedPackageName = expectedPackageName;
    }

    public String getActualPackageName() {
        return actualPackageName;
    }

    public String getExpectedPackageName() {
        return expectedPackageName;
    }
}
