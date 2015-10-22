/*
 * Copyright (c) 2015 Jarrad Hope
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.syng.util;

import android.util.Base64;
import android.util.Log;

import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class Encryption {

    private static final String TAG = "Encryption";

    private static SecureRandom random = new SecureRandom();
    private static String delimiter = "}";
    private static int keyLength = 256;
    private static int saltLength = keyLength / 8;
    private static int iterationCount = 20000;

    public static String encrypt(String text, String password) {

        Log.d(TAG, "Encrypting: " + text);
        String encryptedText = "";
        byte[] salt = generateSalt(saltLength);
        SecretKey key = generateKey(password, salt);
        if (key != null) {
            try {
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                byte[] iv = new byte[cipher.getBlockSize()];
                random.nextBytes(iv);
                IvParameterSpec ivParams = new IvParameterSpec(iv);
                cipher.init(Cipher.ENCRYPT_MODE, key, ivParams);
                byte[] encryptedKey = cipher.doFinal(text.getBytes("UTF-8"));

                encryptedText = Base64.encodeToString(salt, Base64.NO_WRAP);
                encryptedText += delimiter + Base64.encodeToString(iv, Base64.NO_WRAP);
                encryptedText += delimiter + Base64.encodeToString(encryptedKey, Base64.NO_WRAP);
                Log.d(TAG, "Encrypted: " + encryptedText);
                return encryptedText;
            } catch (Exception e) {
                Log.e(TAG, "encrypt(): " + e.toString());
            }
        }
        return null;
    }

    private static byte[] generateSalt(int saltLength) {

        byte[] salt = new byte[saltLength];
        random.nextBytes(salt);
        return salt;
    }

    private static SecretKey generateKey(String password, byte[] salt) {
        try {
            KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, iterationCount, keyLength);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] keyBytes = keyFactory.generateSecret(keySpec).getEncoded();
            return new SecretKeySpec(keyBytes, "AES");
        } catch (Exception e) {
            Log.e(TAG, "generateKey(): " + e.toString());
        }
        return null;
    }

    public static String decrypt(String encryptedText, String password) {

        Log.d(TAG, "Decrypting: " + encryptedText);
        String[] parts = encryptedText.split(Pattern.quote(delimiter));
        byte[] salt = Base64.decode(parts[0], Base64.NO_WRAP);
        byte[] iv =  Base64.decode(parts[1], Base64.NO_WRAP);
        byte[] cipherBytes =  Base64.decode(parts[2], Base64.NO_WRAP);

        SecretKey key = generateKey(password, salt);
        if (key != null) {
            try {
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                IvParameterSpec ivParams = new IvParameterSpec(iv);
                cipher.init(Cipher.DECRYPT_MODE, key, ivParams);
                byte[] decrypted = cipher.doFinal(cipherBytes);
                String decryptedText = new String(decrypted, "UTF-8");
                Log.d(TAG, "Decrypted: " + decryptedText);
                return decryptedText;
            } catch (Exception e) {
                Log.e(TAG, "decrypt(): " + e.toString());
            }
        }

        return null;
    }

}
