package com.sbank.netbanking.service;

import java.time.Instant;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.sbank.netbanking.constants.AppConstants;
import com.sbank.netbanking.dao.SessionDAO;
import com.sbank.netbanking.dao.UserDAO;
import com.sbank.netbanking.dto.UserDTO;
import com.sbank.netbanking.exceptions.ExceptionMessages;
import com.sbank.netbanking.exceptions.TaskException;
import com.sbank.netbanking.model.SessionData;
import com.sbank.netbanking.model.User;
import com.sbank.netbanking.util.UserMapper;

public class LoginService {

    public UserDTO validator(JSONObject json, HttpServletRequest request, HttpServletResponse response) throws TaskException {
        try {
            String userIdString = json.getString("user_id");
            String passwordInput = json.getString("password");
            long epochMilli = Instant.now().toEpochMilli();
            Long userId = Long.parseLong(userIdString);

            UserDAO loginDAO = new UserDAO();
            User user = loginDAO.getUserById(userId); // Getting user data from database

            if (user != null && BcryptService.isPasswordMatch(passwordInput, user.getPassword())) {
                UserMapper userMapper = new UserMapper();
                UserDTO userDataDto = userMapper.toUserDTO(user);

                //  Generate UUID session ID
                String customSessionId = UUID.randomUUID().toString();

                //  Set it in the database
                setSessionData(userId, customSessionId, epochMilli, AppConstants.SESSION_TIMEOUT_MILLIS);

                // Set it as a secure cookie
                Cookie sessionCookie = new Cookie(AppConstants.SESSION_COOKIE_NAME, customSessionId);
                sessionCookie.setHttpOnly(AppConstants.SESSION_COOKIE_HTTP_ONLY);
                sessionCookie.setSecure(AppConstants.SESSION_COOKIE_SECURE);
                sessionCookie.setPath("/");
                response.addCookie(sessionCookie);
                
                
                System.out.println("Generated UUID session ID: " + customSessionId);
                return userDataDto;
            }

        } catch (JSONException e) {
            throw new TaskException(ExceptionMessages.DATA_NOT_FOUND_IN_JSON);
        } catch (NumberFormatException e) {
            throw new TaskException(ExceptionMessages.LONG_OBJECT_CONVERSION_ERROR, e);
        }

        return null;
    }

    
   
    
    
    
    
    
    private void setSessionData( long userId, String customSessionId, long epochMilli, long expiryDuration) throws TaskException {     
 

		SessionData sessionData = new SessionData(); // Data container
		SessionDAO sessionDao = new SessionDAO();

    	sessionData.setUserId(userId);
    	sessionData.setSessionID(customSessionId);
       	sessionData.setStartTime(epochMilli);
    	sessionData.setExpiryDuration(expiryDuration);
    	
    	// Have to set the sesson data object 
    	
    	sessionDao.createDbSession(sessionData);
	
    }
    
    

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}
