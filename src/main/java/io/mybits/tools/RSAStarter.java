package io.mybits.tools;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public class RSAStarter {

    public static void main(String[] args) throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();
        RSAPrivateKey privateKey = (RSAPrivateKey)pair.getPrivate();
        RSAPublicKey publicKey = (RSAPublicKey)pair.getPublic();
        BigInteger modulus = publicKey.getModulus();
        BigInteger publicExponent = publicKey.getPublicExponent();
        BigInteger privateModulus = privateKey.getModulus();
        BigInteger privateExponent = privateKey.getPrivateExponent();
        System.out.println("RSA:\nModulus : " + modulus + "\nPub Exp : " + publicExponent + "\nPri Mod : " + privateModulus + "\nPri Exp : " + privateExponent);
    }
}
