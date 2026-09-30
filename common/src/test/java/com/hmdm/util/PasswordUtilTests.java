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

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>A test suite for {@link PasswordUtil} class.</p>
 */
public class PasswordUtilTests {

    /**
     * <p>Constructs new <code>PasswordUtilTests</code> instance. This implementation does nothing.</p>
     */
    public PasswordUtilTests() {
    }

    @Test
    public void testGenerateTokenFormat() {
        String token = PasswordUtil.generateToken();
        Assert.assertNotNull(token);
        Assert.assertEquals(20, token.length());
        for (int i = 0; i < token.length(); i++) {
            char c = token.charAt(i);
            Assert.assertTrue("unexpected char in token: " + c,
                    (c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'));
        }
    }

    @Test
    public void testGenerateTokenUniqueness() {
        Set<String> tokens = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            Assert.assertTrue("duplicate token generated", tokens.add(PasswordUtil.generateToken()));
        }
    }

    @Test
    public void testTokenGeneratorUsesSecureRandom() throws Exception {
        // generateToken() backs session auth tokens, password-reset tokens and
        // signup verification tokens. java.util.Random is a reversible LCG: its
        // whole future output can be predicted from a single observed token,
        // so the generator backing it must be a CSPRNG.
        Field field = PasswordUtil.class.getDeclaredField("random");
        field.setAccessible(true);
        Assert.assertTrue("token generator must use SecureRandom, not java.util.Random",
                field.get(null) instanceof SecureRandom);
    }
}
