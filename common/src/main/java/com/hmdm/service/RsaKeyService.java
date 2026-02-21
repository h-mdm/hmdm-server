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

package com.hmdm.service;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>A service to use for asymmetric encryption to get sensitive data from the front-end.</p>
 *
 * @author seva
 */
@Singleton
public class RsaKeyService {
    private static final Logger logger = LoggerFactory.getLogger(RsaKeyService.class);

    // Relative to base directory
    private static String pvtKeyFileName = "private.key";
    private static String pubKeyFileName = "public.key";

    private String baseDirectory;

    @Inject
    public RsaKeyService(@Named("base.directory") String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    public boolean generateKeys() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(1024);
            KeyPair kp = kpg.generateKeyPair();
            Key pub = kp.getPublic();
            logger.info("Public key format: {}", pub.getFormat());
            Key pvt = kp.getPrivate();
            logger.info("Private key format: {}", pvt.getFormat());
            if (baseDirectory.equals("")) {
                logger.error("Failed to initialize RSA key: base.directory not initialized");
                return false;
            }
            FileUtils.writeByteArrayToFile(new File(baseDirectory, pubKeyFileName), pub.getEncoded());
            FileUtils.writeByteArrayToFile(new File(baseDirectory, pvtKeyFileName), pvt.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            logger.error("Failed to initialize RSA key: no such algorithm");
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            logger.error("Failed to initialize RSA key: failed to save keys to a file");
            e.printStackTrace();
        }
        return true;
    }

    // Return the private key for using asymmetric encryption at the front-end
    // If it doesn't exist, generate and save it
    public PrivateKey getPrivateKey() {
        File keyFile = new File(baseDirectory, pvtKeyFileName);
        if (!keyFile.exists()) {
            if (!generateKeys()) {
                return null;
            }
        }
        if (!keyFile.exists()) {
            return null;
        }
        try {
            byte[] bytes = FileUtils.readFileToByteArray(keyFile);
            PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(bytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PrivateKey pvt = kf.generatePrivate(ks);
            return pvt;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Return the public key for using asymmetric encryption at the front-end
    // If it doesn't exist, generate and save it
    public PublicKey getPublicKey() {
        File keyFile = new File(baseDirectory, pubKeyFileName);
        if (!keyFile.exists()) {
            if (!generateKeys()) {
                return null;
            }
        }
        if (!keyFile.exists()) {
            return null;
        }
        try {
            byte[] bytes = FileUtils.readFileToByteArray(keyFile);
            X509EncodedKeySpec ks = new X509EncodedKeySpec(bytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PublicKey pub = kf.generatePublic(ks);
            return pub;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String decrypt(byte[] encrypted) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, getPrivateKey());
            cipher.update(encrypted);
            byte[] result = cipher.doFinal();
            return new String(result, StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.error("Failed to decrypt string: {}", e.getMessage());
            return null;
        }
    }
}
