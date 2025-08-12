package com.sbank.netbanking.util;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.sbank.netbanking.auth.CookieEncryptor;
import com.sbank.netbanking.constants.AppConstants;

public class CookieUtil {

    public String[] getSessionIdAndUserId(HttpServletRequest request) {
        final String SESSION_COOKIE_NAME = AppConstants.SESSION_COOKIE_NAME;

        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        for (Cookie cookie : cookies) {
            if (SESSION_COOKIE_NAME.equals(cookie.getName())) {
                try {
                	
                	String encryptedCookieValue = cookie.getValue();
                	CookieEncryptor cookieEncryptor = new CookieEncryptor();
                	String decryptedCookie = cookieEncryptor.decrypt(encryptedCookieValue);
                	
                    String decodedValue = URLDecoder.decode(decryptedCookie, StandardCharsets.UTF_8);
                    return decodedValue.split(":", 2); // returns [sessionUUID, sessionId]
 
                } 
                
                catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }
        return null;
    }
}




















































//
//public class CookieUtil {
//	
//    public String getSessionIdFromCookies(HttpServletRequest request) {
//    	final String SESSION_COOKIE_NAME = "BANK_SESSION_ID";
//    	
//        Cookie[] cookies = request.getCookies();
//        if (cookies == null) return null;
//
//        for (Cookie cookie : cookies) {
//            if (SESSION_COOKIE_NAME.equals(cookie.getName())) {
//                return cookie.getValue();
//            }
//        }
//        return null;
//    }
//	
//
//}
