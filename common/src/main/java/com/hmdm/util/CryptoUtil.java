/*
 * Headwind MDM: Open Source Android MDM Software https://h-mdm.com
 *
 * Copyright (C) 2019 Headwind Solutions LLC (https://h-mdm.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */

package com.hmdm.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.Base64;
import java.util.HexFormat;

public class CryptoUtil {
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    private static final char[] hexArray = "0123456789abcdef".toCharArray();

    public CryptoUtil() {}

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
        return HexFormat.of().withUpperCase().formatHex(digest);
    }

    /**
     * <p>Encodes the specified content into Base-64 URL-safe format according to RFC 3548.</p>
     *
     * @param digest a content to encode.
     *
     * @return a base-64 encoded string representing the specified content.
     */
    public static String getBase64String(byte[] digest) {
        return Base64.getUrlEncoder().encodeToString(digest);
    }

    public static String calculateChecksum(InputStream fileContent) throws NoSuchAlgorithmException, IOException {
        // Calculate checksum
        MessageDigest md = MessageDigest.getInstance("MD5");
        try (InputStream is = new BufferedInputStream(fileContent);
                DigestInputStream dis = new DigestInputStream(is, md)) {
            dis.transferTo(OutputStream.nullOutputStream());
        }

        return HexFormat.of().formatHex(md.digest());
    }

    public static String getSHA1String(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(value.getBytes());
            byte[] digest = md.digest();

            return getHexString(digest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String getDataSignature(String hashSecret, Object data) {
        String s = "";
        try {
            s = JSON_MAPPER.writeValueAsString(data);
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

    public static String randomHexString(int length) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            sb.append(hexArray[random.nextInt(16)]);
        }

        return sb.toString();
    }
}
