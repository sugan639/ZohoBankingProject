package com.sbank.netbanking.crypt;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class SignatureUtil {

 private static final String ALGORITHM = "SHA256withRSA";
 private static final String PROVIDER = "SunRsaSign";

 /**
  * Sign data using private key
  */
 public static String sign(String data, String privateKeyPem) throws GeneralSecurityException {
     byte[] decodedKey = decodePem(privateKeyPem);
     PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);
     KeyFactory keyFactory = KeyFactory.getInstance("RSA", PROVIDER);
     PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

     Signature signature = Signature.getInstance(ALGORITHM, PROVIDER);
     signature.initSign(privateKey);
     signature.update(data.getBytes(StandardCharsets.UTF_8));
     byte[] sigBytes = signature.sign();

     return Base64.getEncoder().encodeToString(sigBytes);
 }

 /**
  * Verify signature using public key
  */
 public static boolean verify(String data, String signatureB64, String publicKeyPem) throws GeneralSecurityException {
     byte[] decodedKey = decodePem(publicKeyPem);
     X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
     KeyFactory keyFactory = KeyFactory.getInstance("RSA", PROVIDER);
     PublicKey publicKey = keyFactory.generatePublic(keySpec);

     Signature signature = Signature.getInstance(ALGORITHM, PROVIDER);
     signature.initVerify(publicKey);
     signature.update(data.getBytes(StandardCharsets.UTF_8));

     byte[] sigBytes = Base64.getDecoder().decode(signatureB64);
     return signature.verify(sigBytes);
 }

 /**
  * Remove PEM headers and decode Base64
  */
 private static byte[] decodePem(String pem) {
     return Base64.getDecoder().decode(
         pem.replaceAll("-----BEGIN (.*)-----", "")
            .replaceAll("-----END (.*)-----", "")
            .replaceAll("\\s", "")
     );
 }
}