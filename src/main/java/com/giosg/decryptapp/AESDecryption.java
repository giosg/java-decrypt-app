package com.giosg.decryptapp;

import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESDecryption {


    public static byte[] urlDecodeAESkeyString(String aesKeyString) {
        aesKeyString = aesKeyString.replaceAll(" ", "");

        int d = aesKeyString.length()  % 4;
        switch (d) {
            case 1:
                throw new IllegalArgumentException("Base64 decoding error");
            case 2:
                aesKeyString += "==";
                break;
            case 3:
                aesKeyString += "=";
                break;
            default:
                break;
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

        cipher.init(Cipher.DECRYPT_MODE, aesKey, iv);

        byte[] decryptedBytes = cipher.doFinal(chipherTextBytes);
        return new String(decryptedBytes);
    }
}
