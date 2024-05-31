package com.giosg.decryptapp;

import javax.crypto.Cipher;
import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;

public class AESDecryption {


    public static byte[] urlDecodeAESkeyString(String aesKeyString) {
        aesKeyString = aesKeyString.replaceAll(" ", "");

        int d = aesKeyString.length()  % 4;
        if (d == 1) {
            throw new IllegalArgumentException("Base64 decoding error");
        } else if (d == 2) {
            aesKeyString += "==";
        } else if (d == 3) {
            aesKeyString += "=";
        }
        return  Base64.getUrlDecoder().decode(aesKeyString);
    }

    public static SecretKey createAESKey(String aesKeyString) {
        byte[] decodedKey = urlDecodeAESkeyString(aesKeyString);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }

    public static String decrypt(String encryptedText, SecretKey aesKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] chipherText = Base64.getDecoder().decode(encryptedText);

        int HEADER_SIZE = 5;
        int AES_BLOCK_SIZE = 16;
        int HMAC_LEN = 20;
        byte[] dataBytes = Arrays.copyOfRange(chipherText, HEADER_SIZE, chipherText.length);
        byte[] ivBytes = Arrays.copyOfRange(dataBytes, 0, AES_BLOCK_SIZE);
        IvParameterSpec iv = new IvParameterSpec(ivBytes);

        byte[] chipherTextBytes = Arrays.copyOfRange(dataBytes, AES_BLOCK_SIZE, dataBytes.length - HMAC_LEN);

        System.out.println("chipherText: " + chipherText.length);
        System.out.println("dataBytes: " + dataBytes.length);
        System.out.println("ivBytes: " + ivBytes.length);
        System.out.println("chipherTextBytes: " + chipherTextBytes.length);

        cipher.init(Cipher.DECRYPT_MODE, aesKey, iv);

        byte[] decryptedBytes = cipher.doFinal(chipherTextBytes);
        return new String(decryptedBytes);
    }
}
