package com.sbank.netbanking.service;

import com.sbank.netbanking.dao.SessionDAO;
import com.sbank.netbanking.exceptions.ExceptionMessages;
import com.sbank.netbanking.exceptions.TaskException;
import com.sbank.netbanking.model.SessionData;
import com.sbank.netbanking.util.CheckTimeout;

public class SessionService {
	
	
	
	public SessionData sessionValidator(String sessionId ) throws TaskException {
		
		if (sessionId == null || sessionId.isEmpty()) {
            System.out.println("Session ID is null or empty.");
            return null;
        }
		
		SessionDAO sessionDao = new SessionDAO();
		SessionData dbSessionData = new SessionData();


    	dbSessionData = sessionDao.getSessionData(sessionId);
    	
    	String dbSessionId = dbSessionData.getSessionID() ;
    	
    	CheckTimeout checkTimeout =  new CheckTimeout(); 
    	if(checkTimeout.isTimeOut(dbSessionData)) {
    		return null;
    	}
    	
        // Sliding session: Update start_time to current time
        boolean updated = sessionDao.updateSessionStartTime(sessionId, System.currentTimeMillis());
        if (!updated) {
            System.out.println("Failed to update session start time.");
            return null;
        }


    	if (dbSessionId != null) {

    		
    	    return dbSessionData;
    	}
    	
    	System.out.println("==============================");
        System.out.println("Data base session ID: "+dbSessionId );
    	System.out.println("==============================");

		
		return null;
	}
	
	
	
	
	public void deleteDbCookies(String sessionId) throws TaskException {
	    if (sessionId == null) {
	        throw new TaskException(ExceptionMessages.NULL_SESSIONID_ERROR);
	    }

	    System.out.println("Reached the session invalidation method. ");
	    
	    SessionDAO sessionDao = new SessionDAO();
	    boolean deleted = sessionDao.deleteSessionBySessionId(sessionId);

	    if (!deleted) {
	        throw new TaskException(ExceptionMessages.DB_SESSION_DATA_NOT_FOUND + sessionId);
	    }
	    
	    System.out.println("Session cookie deletion method completed !");


	}

	



}






















