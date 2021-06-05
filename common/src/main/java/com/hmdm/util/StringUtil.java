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
 * <p>An utility class providing various string manipulation methods.</p>
 *
 * @author isv
 */
public final class StringUtil {

    /**
     * <p>Constructs new <code>StringUtil</code> instance. This implementation does nothing.</p>
     */
    private StringUtil() {
    }

    /**
     * <p>Removes the specified string from trailing positions in specified text.</p>
     *
     * @param text a text to check.
     * @param st a string to be stripped off from the specified text.
     * @return an original text with specified string stripped off from the trailing places.
     */
    public static String stripOffTrailingCharacter(String text, String st) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        if (text.startsWith(st)) {
            text = text.substring(st.length());
        }
        if (text.endsWith(st)) {
            text = text.substring(0, text.length() - st.length());
        }

        return text;
    }

    public static String jsonEscape(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    public static boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }
}
