package com.giosg.decryptapp;

import java.io.FileReader;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.Security;
import java.util.Base64;

import javax.crypto.Cipher;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;

public class PEMKeyDecryption {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static PrivateKey loadPrivateKey(String pemFilePath, String pemPassword) throws IOException {
        JcaPEMKeyConverter converter;
        PrivateKeyInfo keyInfo;
        try (PEMParser pemParser = new PEMParser(new FileReader(pemFilePath))) {
            converter = new JcaPEMKeyConverter().setProvider("BC");
            Object object = pemParser.readObject();
            keyInfo = getPrivateKeyInfo(object, pemPassword);
        }
        return converter.getPrivateKey(keyInfo);
    }

    public static PrivateKeyInfo getPrivateKeyInfo(Object pemObject, String password) throws IOException {
        if (pemObject instanceof PEMEncryptedKeyPair) {
            PEMEncryptedKeyPair encryptedKeyPair = (PEMEncryptedKeyPair) pemObject;
            JcePEMDecryptorProviderBuilder decryptorProviderBuilder = new JcePEMDecryptorProviderBuilder();
            PrivateKeyInfo privateKeyInfo = encryptedKeyPair.decryptKeyPair(decryptorProviderBuilder.build(password.toCharArray())).getPrivateKeyInfo();
            return privateKeyInfo;
        } else {
            throw new IllegalArgumentException("The provided PEM file does not contain an encrypted key pair.");
        }
    }

    public static byte[] decrypt(String base64EncryptedText, PrivateKey privateKey) throws Exception {
        byte[] encryptedBytes = Base64.getDecoder().decode(base64EncryptedText);

        String chipherSpec = "RSA/ECB/OAEPWithSHA-1AndMGF1Padding";
        Cipher cipher = Cipher.getInstance(chipherSpec, "BC");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(encryptedBytes);
    }
}