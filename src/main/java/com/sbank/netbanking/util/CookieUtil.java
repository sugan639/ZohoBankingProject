package com.sbank.netbanking.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class CookieUtil {
	
    public String getSessionIdFromCookies(HttpServletRequest request) {
    	final String SESSION_COOKIE_NAME = "BANK_SESSION_ID";
    	
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        for (Cookie cookie : cookies) {
            if (SESSION_COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
	

}
