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

import com.hmdm.rest.filter.BaseIPFilter;
import org.junit.Assert;
import org.junit.Test;

/**
 * <p>A test suite for {@link BaseIPFilter} class.</p>
 *
 * @author seva
 */
public class IPFilterTests {

    /**
     * <p>Constructs new <code>IPFilterTests</code> instance. This implementation does nothing.</p>
     */
    public IPFilterTests() {
    }

    @Test
    public void testMatch() {
        BaseIPFilter filter = new BaseIPFilter("192.168.0.0/26,1.0.0.0/1,11.11.11.11/32,10.1.2.3", "", "");
        Assert.assertEquals(filter.match("192.168.0.1"), true);
        Assert.assertEquals(filter.match("213.110.2.1"), false);
    }
}
