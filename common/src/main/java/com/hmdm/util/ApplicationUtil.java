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

import org.apache.commons.lang3.StringUtils;

/**
 * <p>An utility class for manipulating with application data.</p>
 *
 * @author isv
 */
public final class ApplicationUtil {

    /**
     * <p>Constructs new <code>ApplicationUtil</code> instance. This implementation does nothing.</p>
     */
    private ApplicationUtil() {
    }

    /**
     * <p>Compares the specified application versions.</p>
     *
     * @param version1 a first application version to compare.
     * @param version2 a second application version to compare.
     * @return a comparison result. 0 - if versions are equal, -1 - if first version is considered to be less than the
     *         second one; 1 - otherwise.
     */
    public static int compareVersions(String version1, String version2) {
        if (version1 == null && version2 == null) {
            return 0;
        }
        if (version1 != null && version2 == null) {
            return 1;
        }
        if (version1 == null) {
            return -1;
        }

        final String[] split1 = normalizeVersion(version1).split("\\.");
        final String[] split2 = normalizeVersion(version2).split("\\.");

        final StringBuilder b1 = new StringBuilder();
        final StringBuilder b2 = new StringBuilder();
        final int N = Math.max(split1.length, split2.length);
        for (int i = 0; i < N; i++) {
            String s1 = i < split1.length ? split1[i] : "";
            if (s1.isEmpty()) {
                s1 = "0";
            }

            b1.append(StringUtils.leftPad(s1, 10, "0"));

            String s2 = i < split2.length ? split2[i] : "";
            if (s2.isEmpty()) {
                s2 = "0";
            }
            b2.append(StringUtils.leftPad(s2, 10, "0"));
        }

        int result = b1.toString().compareTo(b2.toString());
        if (result < 0) {
            return -1;
        } else if (result > 0) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * <p>Normalizes the specified string for comparison. Strips off all non-digit and non-dot characters from it.</p>
     *
     * @param version a version text to normalize.
     * @return a normalized version text.
     */
    public static String normalizeVersion(String version) {
        return (version == null ? "" : version).replaceAll("[^\\d.]", "");
    }
}
