package com.sbank.netbanking.service;

import java.util.Enumeration;

import javax.servlet.http.HttpSession;

import com.sbank.netbanking.dao.SessionDAO;
import com.sbank.netbanking.exceptions.ExceptionMessages;
import com.sbank.netbanking.exceptions.TaskException;
import com.sbank.netbanking.model.SessionData;

public class SessionService {
	
	
	
	public boolean sessionValidator(HttpSession session ) throws TaskException {
		
		
		
		SessionDAO sessionDao = new SessionDAO();
		SessionData userSessionData = new SessionData();
		SessionData dbSessionData = new SessionData();
		
		
		userSessionData = (SessionData) session.getAttribute("SessionData");

		if(session!=null) {
            System.out.println( "Session is not NULL on sessionValidator");

		}
		
		
		
		long userId = userSessionData.getUserId();
		String userSessionId = userSessionData.getSessionID();  // These two lines are causing NULLPointer Exception
		
		
    	System.out.println("UserID: " + userId);
    	System.out.println("sessionID: " + userSessionId);

    	dbSessionData = sessionDao.getSessionData(userId);
    	
    	String dbSessionId = dbSessionData.getSessionID() ;
		

    	if (userSessionId != null && userSessionId.equals(dbSessionId)) {
    	    return true;
    	}
    	
    	System.out.println("==============================");
        System.out.println("Data base session ID: "+dbSessionId );
        System.out.println("User session ID: "+userSessionId);
    	System.out.println("==============================");

		
		return false;
	}
	
	public void invalidateDbSession(HttpSession session) throws TaskException {
	    if (session == null) {
	        throw new TaskException(ExceptionMessages.NULL_SESSION_ERROR);
	    }

	    SessionData sessionData = (SessionData) session.getAttribute("SessionData");

	    if (sessionData == null) {
	        throw new TaskException(ExceptionMessages.USER_SESSION_DATA_NOT_FOUND);
	    }

	    long userId = sessionData.getUserId();

	    SessionDAO sessionDao = new SessionDAO();
	    boolean deleted = sessionDao.deleteSessionByUserId(userId);

	    if (!deleted) {
	        throw new TaskException(ExceptionMessages.DB_SESSION_DATA_NOT_FOUND + userId);
	    }

	    // Invalidate in-memory session too
	    session.invalidate();
	}

	
	public void printSessionData(HttpSession session) {
	    if (session == null) {
	        System.out.println("Session is null.");
	        return;
	    }

	    System.out.println("Session ID: " + session.getId());

	    Enumeration<String> attributeNames = session.getAttributeNames();

	    if (!attributeNames.hasMoreElements()) {
	        System.out.println("No attributes in session.");
	        return;
	    }

	    System.out.println("Session Attributes:");
	    while (attributeNames.hasMoreElements()) {
	        String name = attributeNames.nextElement();
	        Object value = session.getAttribute(name);
	        System.out.println(name + " = " + value);
	    }
	}



}
