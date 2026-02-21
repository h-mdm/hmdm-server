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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>A test suite for {@link ApplicationUtil} class.</p>
 *
 * @author isv
 */
public class ApplicationUtilTests {

    /**
     * <p>Constructs new <code>ApplicationUtilTests</code> instance. This implementation does nothing.</p>
     */
    public ApplicationUtilTests() {}

    @Test
    public void testEquality() {
        Assertions.assertEquals(0, ApplicationUtil.compareVersions("1.0", "1.0"), "Should be equal");
        Assertions.assertEquals(0, ApplicationUtil.compareVersions("1.0", "1.00"), "Should be equal");
        Assertions.assertEquals(0, ApplicationUtil.compareVersions("1.01", "1.1"), "Should be equal");
        Assertions.assertEquals(0, ApplicationUtil.compareVersions("1.01", "1.00001"), "Should be equal");
        Assertions.assertEquals(0, ApplicationUtil.compareVersions("1", "1"), "Should be equal");
        Assertions.assertEquals(0, ApplicationUtil.compareVersions("0", "0"), "Should be equal");
        Assertions.assertEquals(0, ApplicationUtil.compareVersions("0.abc", "0.xyz"), "Should be equal");
    }

    @Test
    public void testLess() {
        Assertions.assertEquals(-1, ApplicationUtil.compareVersions("1.1", "2.0"), "Should be less");
        Assertions.assertEquals(-1, ApplicationUtil.compareVersions("1.3", "1.11"), "Should be less");
        Assertions.assertEquals(-1, ApplicationUtil.compareVersions("1.03", "1.11"), "Should be less");
        Assertions.assertEquals(-1, ApplicationUtil.compareVersions("1.11", "1.033"), "Should be less");
        Assertions.assertEquals(-1, ApplicationUtil.compareVersions("1.1", "1.1.12"), "Should be less");
        Assertions.assertEquals(-1, ApplicationUtil.compareVersions("1.1.11", "1.1.12"), "Should be less");
        Assertions.assertEquals(-1, ApplicationUtil.compareVersions("1.1.12", "1.1.110"), "Should be less");
        Assertions.assertEquals(-1, ApplicationUtil.compareVersions("1.a.12", "1.1.110"), "Should be less");
    }

    @Test
    public void testGreater() {
        Assertions.assertEquals(1, ApplicationUtil.compareVersions("2.0", "1.1"), "Should be greater");
        Assertions.assertEquals(1, ApplicationUtil.compareVersions("1.11", "1.3"), "Should be greater");
        Assertions.assertEquals(1, ApplicationUtil.compareVersions("1.11", "1.03"), "Should be greater");
        Assertions.assertEquals(1, ApplicationUtil.compareVersions("1.033", "1.11"), "Should be greater");
        Assertions.assertEquals(1, ApplicationUtil.compareVersions("1.1.12", "1.1"), "Should be greater");
        Assertions.assertEquals(1, ApplicationUtil.compareVersions("1.1.12", "1.1.11"), "Should be greater");
        Assertions.assertEquals(1, ApplicationUtil.compareVersions("1.1.110", "1.1.12"), "Should be greater");
        Assertions.assertEquals(1, ApplicationUtil.compareVersions("1.1.110", "1.a.12"), "Should be greater");
        Assertions.assertEquals(1, ApplicationUtil.compareVersions("1.03", "1.2.12"), "Should be greater");
    }

    @Test
    public void testNormalization() {
        Assertions.assertEquals(
                "12.334.78", ApplicationUtil.normalizeVersion("a12.334tyz.78x"), "Incorrect version normalization");
        Assertions.assertEquals("1.03", ApplicationUtil.normalizeVersion("1.03"), "Incorrect version normalization");
        Assertions.assertEquals(
                "1.03.011", ApplicationUtil.normalizeVersion("1.03.011"), "Incorrect version normalization");
        Assertions.assertEquals("1.03", ApplicationUtil.normalizeVersion("1.03-a"), "Incorrect version normalization");
        Assertions.assertEquals("1.03", ApplicationUtil.normalizeVersion("1.03a"), "Incorrect version normalization");
        Assertions.assertEquals("1.03", ApplicationUtil.normalizeVersion("v1.03"), "Incorrect version normalization");
        Assertions.assertEquals("103", ApplicationUtil.normalizeVersion("v103"), "Incorrect version normalization");
        Assertions.assertEquals("", ApplicationUtil.normalizeVersion("aaaa"), "Incorrect version normalization");
        Assertions.assertEquals("1", ApplicationUtil.normalizeVersion("aaaa1"), "Incorrect version normalization");
        Assertions.assertEquals("1.", ApplicationUtil.normalizeVersion("1."), "Incorrect version normalization");
    }
}
