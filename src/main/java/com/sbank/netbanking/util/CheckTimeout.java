package com.sbank.netbanking.util;

import com.sbank.netbanking.exceptions.TaskException;
import com.sbank.netbanking.model.SessionData;
import com.sbank.netbanking.service.SessionService;

public class CheckTimeout {
	
	public boolean isTimeOut(SessionData sessionData, Long sessionId) throws TaskException {
		
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
		
        
        // If timed out, then we delete the session data of that sessionId
        
        SessionService sessionService = new SessionService();
        sessionService.deleteDbCookies(sessionId);
		
	
		return true;
		

		
	}

}



