package com.sbank.netbanking.crypt;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;

public class SignatureUtil {


    private final String ALGORITHM = "SHA256withRSA";

    public String encrypt(String data, PrivateKey publicKey) throws Exception {
        try {
        // Encrypt with Public Key
            Signature signer = Signature.getInstance(ALGORITHM);
            signer.initSign(publicKey);
            signer.update(data.getBytes(StandardCharsets.UTF_8));
            byte[] signature = signer.sign();
            return Base64.getEncoder().encodeToString(signature);
        }
        
        catch (Exception e) {
        	e.printStackTrace();
        }
		return null;
    }
        


    public boolean verify(String data, String signatureBase64, PublicKey publicKey) throws Exception {
        Signature verifier = Signature.getInstance(ALGORITHM);
        verifier.initVerify(publicKey);
        verifier.update(data.getBytes(StandardCharsets.UTF_8));

        byte[] signature = Base64.getDecoder().decode(signatureBase64);
        return verifier.verify(signature);
    }


}