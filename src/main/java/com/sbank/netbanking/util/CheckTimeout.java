package com.sbank.netbanking.util;

import com.sbank.netbanking.model.SessionData;

public class CheckTimeout {
	
	public boolean isTimeOut(SessionData sessionData) {
		
		if (sessionData == null) {
            return true; // Treat null session as timed out
        }

        long sessionStartTime = sessionData.getStartTime();
        long expiryTime = sessionData.getExpiryDuration();
        long currentTime = System.currentTimeMillis();

        long loggedInDuration = (currentTime - sessionStartTime);
        
        System.out.println("Logged in duration: "+ loggedInDuration);
        
        if( loggedInDuration < expiryTime) {
        	return false;
        }
		
		
		
		return true;
		

		
	}

}
