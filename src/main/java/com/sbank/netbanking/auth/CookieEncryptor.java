package com.sbank.netbanking.auth;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.sbank.netbanking.constants.AppConstants;
import com.sbank.netbanking.exceptions.ExceptionMessages;
import com.sbank.netbanking.exceptions.TaskException;

public class CookieEncryptor {

    private final String ENCRYPTION_ALGO = "AES/GCM/NoPadding";
    private final int GCM_TAG_LENGTH = 128;
    private final int IV_LENGTH = 12; // 96 bits

    private  final byte[] SECRET = AppConstants.ENCRYPTOR_SECRET_KEY.getBytes(); // 16 bytes

    public String encrypt(String plainText) throws TaskException  {
    	
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        try {
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGO);
        SecretKeySpec keySpec = new SecretKeySpec(SECRET, "AES");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);

        byte[] encrypted = cipher.doFinal(plainText.getBytes());
        byte[] encryptedWithIv = new byte[iv.length + encrypted.length];

        System.arraycopy(iv, 0, encryptedWithIv, 0, iv.length);
        System.arraycopy(encrypted, 0, encryptedWithIv, iv.length, encrypted.length);

        return Base64.getEncoder().encodeToString(encryptedWithIv);
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException |InvalidKeyException | InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new TaskException(ExceptionMessages.ENCRYPTION_ERROR,e);
		}
    }

    
    public String decrypt(String encryptedData) throws TaskException  {
        byte[] decoded = Base64.getDecoder().decode(encryptedData);
        byte[] iv = new byte[IV_LENGTH];
        byte[] cipherText = new byte[decoded.length - IV_LENGTH];

        System.arraycopy(decoded, 0, iv, 0, IV_LENGTH);
        System.arraycopy(decoded, IV_LENGTH, cipherText, 0, cipherText.length);

        try {
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGO);
        SecretKeySpec keySpec = new SecretKeySpec(SECRET, "AES");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);

        byte[] decrypted = cipher.doFinal(cipherText);
        return new String(decrypted);
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException |InvalidKeyException | InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new TaskException(ExceptionMessages.DECRYPT_ERROR,e);
        }
    }
}
