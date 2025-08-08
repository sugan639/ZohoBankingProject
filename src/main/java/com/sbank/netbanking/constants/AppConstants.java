package com.sbank.netbanking.constants;

import java.util.logging.Level;

public class AppConstants {

	// Timeout constants
	public static final long SESSION_TIMEOUT_MILLIS = 30 * 60 * 1000L; // 30 minutes
    public static final String SESSION_COOKIE_NAME = "BANK_SESSION_ID";
    public static final boolean SESSION_COOKIE_SECURE = true; 
    public static final boolean SESSION_COOKIE_HTTP_ONLY = true;
    
    // Logging
    public static final String LOG_FILE_PATH = "Banking_App/src/main/log/bankingapp.log";
    public static final boolean LOG_APPEND_MODE = true;
    public static final Level LOG_LEVEL = Level.INFO;

    // Add other constants here
    
    // Redis keys
    public static final String SESSION_DATA_KEY = "sessionDataCache";
    public static final String ENCRYPTOR_SECRET_KEY = " mSuperSecretKey";
	public static final String BANK_NAME = "ZOHO";
	
    public static final String HMAC_ALGO = "HmacSHA256";


    
}
