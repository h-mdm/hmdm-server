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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.BaseEncoding;
import com.hmdm.rest.json.Response;
import com.hmdm.rest.json.SyncResponseInt;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CryptoUtil {

    private static final char[] hexArray = "0123456789abcdef".toCharArray();

    public CryptoUtil() {
    }

    public static String getMD5String(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(value.getBytes(StandardCharsets.UTF_8));
            byte[] digest = md.digest();
            return getHexString(digest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String getHexString(byte[] digest) {
        char[] hexChars = new char[digest.length * 2];

        for(int i = 0; i < digest.length; ++i) {
            int v = digest[i] & 255;
            hexChars[i * 2] = hexArray[v >>> 4];
            hexChars[i * 2 + 1] = hexArray[v & 15];
        }

        return (new String(hexChars)).toUpperCase();
    }

    /**
     * <p>Encodes the specified content into Base-64 URL-safe format according to RFC 3548.</p>
     *
     * @param digest a content to encode.
     * @return a base-64 encoded string representing the specified content.
     */
    public static String getBase64String(byte[] digest) {
        String hashString = BaseEncoding.base64Url().encode(digest);
        return hashString;
    }

    public static String calculateChecksum(InputStream fileContent) throws NoSuchAlgorithmException, IOException {
        // Calculate checksum
        MessageDigest md = MessageDigest.getInstance("MD5");
        try (InputStream is = new BufferedInputStream(fileContent);
             DigestInputStream dis = new DigestInputStream(is, md)) {
            /* Read decorated stream (dis) to EOF as normal... */
            int b;
            while ((b = dis.read()) != -1) {
                // digest will consume the content when read() called
            }
        }

        // to calculate message digest of the input string
        // returned as array of byte
        byte[] digest = md.digest();

        // Convert byte array into signum representation
        BigInteger no = new BigInteger(1, digest);

        // Convert message digest into hex value
        String hashtext = no.toString(16);

        // Add preceding 0s to make it 32 bit
        while (hashtext.length() < 32) {
            hashtext = "0" + hashtext;
        }

        return hashtext;
    }

    public static String getSHA1String(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update( value.getBytes());
            byte[] digest = md.digest();

            char[] hexChars = new char[digest.length * 2];
            for (int i = 0; i < digest.length; i++) {
                int v = digest[i] & 0xFF;
                hexChars[i * 2] = hexArray[v >>> 4];
                hexChars[i * 2 + 1] = hexArray[v & 0x0F];
            }
            return new String(hexChars).toUpperCase();
        } catch (Exception e) {
            throw new RuntimeException( e );
        }
    }

    public static String getDataSignature(String hashSecret, Object data) {
        ObjectMapper objectMapper = new ObjectMapper();
        String s = "";
        try {
            s = objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        s = s.replaceAll("\\s", "");
        String signature = CryptoUtil.getSHA1String(hashSecret + s);
        return signature;
    }

    public static boolean checkRequestSignature(String signature, String value) {
        if (signature == null) {
            return false;
        }
        try {
            String goodSignature = CryptoUtil.getSHA1String(value);
            if (!signature.equalsIgnoreCase(goodSignature)) {
                return false;
            }
        } catch (Exception e) {
        }
        return true;
    }
}
