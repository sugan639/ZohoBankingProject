package com.sbank.netbanking.service;

import javax.servlet.http.HttpSession;

import com.sbank.netbanking.dao.SessionDAO;
import com.sbank.netbanking.exceptions.TaskException;
import com.sbank.netbanking.model.SessionData;

public class SessionService {
	
	
	
	public boolean sessionValidator(HttpSession session ) throws TaskException {
		
		
		
		SessionDAO sessionDao = new SessionDAO();
		SessionData userSessionData = new SessionData();
		
		
		userSessionData = (SessionData) session.getAttribute("SessionData");

		if(session!=null) {
            System.out.println( "Session is not NULL on sessionValidator");

		}
		
//		long userId = userSessionData.getUserId();
//	String userSessionId = userSessionData.getSessionID();  // These two lines are causing NULLPointer Exception
		
//		
//    	System.out.println("UserID: " + userId);
//    	System.out.println("sessionID: " + userSessionId);
//
//    	dbSessionData = sessionDao.getSessionData(userId);
//    	
//    	String dbSessionId = dbSessionData.getSessionID() ;
//		if(dbSessionId == userSessionId ) {
//			return true;
//		}
//		
//    	System.out.println("==============================");
//        System.out.println("Data base session ID: "+dbSessionId );
//        System.out.println("User session ID: "+userSessionId);
//    	System.out.println("==============================");

		
		return false;
	
	
	
	
	
	
	
	}


}
