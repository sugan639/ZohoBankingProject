package com.sbank.netbanking.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.sbank.netbanking.auth.CookieEncryptor;
import com.sbank.netbanking.constants.AppConstants;
import com.sbank.netbanking.dao.SessionDAO;
import com.sbank.netbanking.dao.UserDAO;
import com.sbank.netbanking.dto.UserDTO;
import com.sbank.netbanking.exceptions.ExceptionMessages;
import com.sbank.netbanking.exceptions.TaskException;
import com.sbank.netbanking.model.SessionData;
import com.sbank.netbanking.model.User;
import com.sbank.netbanking.util.CheckTimeout;
import com.sbank.netbanking.util.PojoJsonConverter;
import com.sbank.netbanking.util.UserMapper;
import com.sbank.redis.util.RedisUtil;

public class LoginService {

    public UserDTO validator(JSONObject json, HttpServletRequest request, HttpServletResponse response) throws TaskException {
        try {
        	RedisUtil redisUtil = new RedisUtil();
            String userIdString = json.getString("user_id");
            String passwordInput = json.getString("password");
            long epochMilli = Instant.now().toEpochMilli();
            Long userId = Long.parseLong(userIdString);
            
            UserDAO loginDAO = new UserDAO();
            User user = loginDAO.getUserById(userId); // Getting user data from database

            if (user != null && BcryptService.isPasswordMatch(passwordInput, user.getPassword())) {
                UserMapper userMapper = new UserMapper();
                UserDTO userDataDto = userMapper.toUserDTO(user);
                
                if(!userDataDto.isMultipleSessionAllowed()) {

                	if(userSessionAlreadyAvailable(userId)) {
                		return null;
                	}
                }
                
                //  Generate UUID session ID
                String customSessionId = UUID.randomUUID().toString();
                String hashedSessionId = BcryptService.hashPassword(customSessionId);
                
                //  Set it in the database with hashed UUID
                SessionData sessionData = setSessionData(userId, hashedSessionId, epochMilli, AppConstants.SESSION_TIMEOUT_MILLIS);
            	
        		SessionDAO sessionDao = new SessionDAO();
        		
                long id = sessionDao.createDbSession(sessionData);

                // Setting session data in redis cache
                PojoJsonConverter pojoJsonConverter = new PojoJsonConverter();
            	JSONObject sessionJSON = pojoJsonConverter.pojoToJson(sessionData);
            	String sessionJSONString = sessionJSON.toString();
            	String idStr = String.valueOf(id);
            	redisUtil.setCache(idStr, sessionJSONString);
            	String combinedValue = customSessionId + ":" + idStr;

            	
            	// URL-encode to make it cookie-safe
            	String encodedValue = URLEncoder.encode(combinedValue, StandardCharsets.UTF_8);
            	CookieEncryptor cookieEncryptor = new CookieEncryptor(); 
                String encryptedCookie = cookieEncryptor.encrypt(encodedValue);
                
                // Set it as a secure cookie with custom session ID
                Cookie sessionCookie = new Cookie(AppConstants.SESSION_COOKIE_NAME, encryptedCookie);
                sessionCookie.setHttpOnly(AppConstants.SESSION_COOKIE_HTTP_ONLY);
                sessionCookie.setSecure(AppConstants.SESSION_COOKIE_SECURE);
                sessionCookie.setPath("/");
                response.addCookie(sessionCookie);
                
                return userDataDto;
            }

        } catch (JSONException e) {
            throw new TaskException(ExceptionMessages.DATA_NOT_FOUND_IN_JSON);
        } catch (NumberFormatException e) {
            throw new TaskException(ExceptionMessages.USER_VALIDATION_FAILED, e);
        }
  

        return null;
    }

    
   
    
    
    
    
    
    private SessionData setSessionData( long userId, String customSessionId, long epochMilli, long expiryDuration) throws TaskException {     
 

		SessionData sessionData = new SessionData(); // Data container

    	sessionData.setUserId(userId);
    	sessionData.setSessionID(customSessionId);
       	sessionData.setStartTime(epochMilli);
    	sessionData.setExpiryDuration(expiryDuration);
    	
    	
    	return sessionData;
	
    }
    
    
    

    private boolean userSessionAlreadyAvailable(Long UserId) throws TaskException {
		SessionDAO sessionDao =  new SessionDAO();
		List<SessionData> sessionList = sessionDao.checkSessionByUserId(UserId);
    	CheckTimeout checkTimeout =  new CheckTimeout(); 
    	RedisUtil redisUtil = new RedisUtil();
		
		
    	if(sessionList==null || sessionList.isEmpty()) {
    		return false;
    	}
    	
    	
    	boolean validSessionAvailable = false;
    	for (SessionData sessionData : sessionList) {
    		long sessionId = sessionData.getId(); 
		    System.out.println("Session ID to be deleted: "+ sessionId);

    		if(!checkTimeout.isTimeOut(sessionData, sessionId)) {
    			validSessionAvailable =  true;
    		}
    		else {
    			String sessionIdStr = String.valueOf(sessionId);
    			redisUtil.deleteSession(sessionIdStr);
    		}
    		
    	 
    	}
    	
    	return validSessionAvailable;
    	
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}
