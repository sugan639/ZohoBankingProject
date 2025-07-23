package com.sbank.netbanking.service;

import com.sbank.netbanking.encryption.BCrypt;

public final class BcryptService {

    private BcryptService() {
        // Prevent instantiation
    }

    /**
     * Verifies if the plain password matches the hashed password.
     *
     * @param plainPassword The user's raw password (e.g., from login form)
     * @param hashedPassword The BCrypt hashed password from DB
     * @return true if match, false otherwise
     */
    public static boolean isPasswordMatch(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }

    /**
     * Hash a plain password with BCrypt.
     *
     * @param plainPassword the raw password to hash
     * @return hashed password string
     */
    public static String hashPassword(String plainPassword) {
    	if(plainPassword== null) {
    		return null;
    	}
        String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
        return hashedPassword;
    }
}

