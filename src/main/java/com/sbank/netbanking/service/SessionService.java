package com.sbank.netbanking.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.sbank.netbanking.dao.SessionDAO;
import com.sbank.netbanking.exceptions.ExceptionMessages;
import com.sbank.netbanking.exceptions.TaskException;
import com.sbank.netbanking.model.SessionData;
import com.sbank.netbanking.util.CheckTimeout;
import com.sbank.netbanking.util.CookieUtil;
import com.sbank.netbanking.util.RequestJsonConverter;
import com.sbank.redis.util.RedisUtil;

public class SessionService {
	
	
	
	public SessionData sessionValidator(String cookieSessionUUID, String sessionId ) throws TaskException {
		
		RedisUtil redisUtil = new RedisUtil();
    	CheckTimeout checkTimeout =  new CheckTimeout(); 
		SessionDAO sessionDao = new SessionDAO();

		
		if (sessionId == null | cookieSessionUUID == null) {
            System.out.println("Session identifier is null or empty.");
            return null;
        }
		

	    long sessionIdLong = Long.parseLong(sessionId);

		// 1. Getting session data from redis cache
		String sessionJSONString =  redisUtil.getSessionData(sessionId);
	
		if(sessionJSONString!= null)
		{
			JSONObject json = new JSONObject(sessionJSONString); 
		    SessionData sessionData = new SessionData();
		    sessionData.setSessionID(json.getString("sessionID"));
		    sessionData.setUserId(json.getLong("userId"));
		    sessionData.setStartTime(json.getLong("startTime"));
		    sessionData.setExpiryDuration(json.getLong("expiryDuration"));
			System.out.println(sessionData.toString());
			
			String cacheSessionUUID = sessionData.getSessionID();
			
			if(!BcryptService.isPasswordMatch(cookieSessionUUID, cacheSessionUUID )) {
				return null;
			}
		    
		    if(checkTimeout.isTimeOut(sessionData, sessionIdLong)) {
	    		return null;
	    	}
		    
	           // Sliding session: Update start_time to current time
            boolean updated = sessionDao.updateSessionStartTime(sessionIdLong, System.currentTimeMillis());
            
            if (!updated) {
                return null;
            }
		    
		    return sessionData;
		    
		}
		

		// 2. Hitting DB to get session data
		SessionData dbSessionData = new SessionData();
    	dbSessionData = sessionDao.getSessionData(sessionIdLong);
    	if(dbSessionData != null) {    	
        	String dbSessionId = dbSessionData.getSessionID() ;
			if(!BcryptService.isPasswordMatch("72663501-2334-4ff3-8d87-fccbdfdbbabb", dbSessionId )) {
				return null;
			}

        	if(checkTimeout.isTimeOut(dbSessionData, sessionIdLong)) {
        		return null;
        	}  	
            // Sliding session: Update start_time to current time
            boolean updated = sessionDao.updateSessionStartTime(sessionIdLong, System.currentTimeMillis());
            if (!updated) {
                return null;
            }
            
        	if (dbSessionId != null) {
        		
        	    return dbSessionData;
        	}       	
    	}
	
		return null;
	}
	
	
	public void deleteDbCookies(Long id) throws TaskException {
	    if (id == null) {
	        throw new TaskException(ExceptionMessages.NULL_SESSIONID_ERROR);
	    }

	    SessionDAO sessionDao = new SessionDAO();
	    boolean deleted = sessionDao.deleteSessionBySessionId(id);

	    if (!deleted) {
	        throw new TaskException(ExceptionMessages.DB_SESSION_DATA_NOT_FOUND + id);
	    }
	    

	}
	
	// Clears all session except the users current session
	public void clearAllUserSession(HttpServletRequest req, HttpServletResponse res) {

        RequestJsonConverter jsonConverter = new RequestJsonConverter();


        try {
        CookieUtil cookieUtil = new CookieUtil();
        String[] values = cookieUtil.getSessionIdAndUserId(req);
        JSONObject json = jsonConverter.convertToJson(req);

        Boolean clearAllSession = json.has("clear_all_session") ? json.getBoolean("clear_all_session") : false;
        
        if(clearAllSession) {
        String sessionidString = values[1];  
        
     	SessionData sessionData = (SessionData) req.getAttribute("sessionData");
     	
     	long sessionId = Long.parseLong(sessionidString);
        long userId = sessionData.getUserId();
        
        SessionDAO sessionDao = new SessionDAO();
        int clearedSession = sessionDao.clearAllUserSession(userId , sessionId);

        System.out.println("Cleared sessios: "+clearedSession);
        }
        }
        catch(TaskException e) {
        	e.printStackTrace();
        }
	}
	
	
	
	
}

















