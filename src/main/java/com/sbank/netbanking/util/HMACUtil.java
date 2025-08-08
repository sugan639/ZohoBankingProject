
package com.sbank.netbanking.util;

import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.sbank.netbanking.constants.AppConstants;

public class HMACUtil {


    /**
     * Generates HMAC using given message and secret key
     */
    public static String generateHMAC(String message, String secretKey) {
        try {
            Mac sha256_HMAC = Mac.getInstance(AppConstants.HMAC_ALGO);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), AppConstants.HMAC_ALGO);
            sha256_HMAC.init(secretKeySpec);

            byte[] hmacBytes = sha256_HMAC.doFinal(message.getBytes());
            String hmac = Base64.getEncoder().encodeToString(hmacBytes);

            return hmac;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Verifies that the provided HMAC matches with the computed one
     */
    public static boolean verifyHMAC(String message, String providedHmac, String secretKey) {
        String computedHmac = generateHMAC(message, secretKey);
        return computedHmac != null && computedHmac.equals(providedHmac);
    }
}
