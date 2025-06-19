package com.sbank.netbanking.service;

import java.time.Instant;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.json.JSONException;
import org.json.JSONObject;

import com.sbank.netbanking.dao.SessionDAO;
import com.sbank.netbanking.dao.UserDAO;
import com.sbank.netbanking.dto.UserDTO;
import com.sbank.netbanking.exceptions.ExceptionMessages;
import com.sbank.netbanking.exceptions.TaskException;
import com.sbank.netbanking.model.SessionData;
import com.sbank.netbanking.model.User;
import com.sbank.netbanking.model.User.Role;
import com.sbank.netbanking.util.UserMapper;

public class LoginService {

    public UserDTO validator(JSONObject json, HttpServletRequest request ) throws  TaskException {

		HttpSession session = request.getSession(); 

    	
    	try {
    		String userIdString = json.getString("user_id");
            String passwordInput = json.getString("password");
            Role role = Role.valueOf(json.getString("role"));
            long epochMilli = Instant.now().toEpochMilli();
    	    Long userId = Long.parseLong(userIdString);

    	    
	    	UserDAO loginDAO = new UserDAO();
	        User user = null;
	        UserDTO userDataDto = null;
	        UserMapper userMapper =  new UserMapper();

	        // Getting user table data
	        user = loginDAO.getUserById(userId);	
            
            
            // Checking against correct password
            
            if (user != null && BcryptService.isPasswordMatch(passwordInput, user.getPassword())) { 
            	userDataDto = userMapper.toUserDTO(user);
            	
            	
               	setSessionData(session,userId, role, epochMilli);
            
            	System.out.println("Reached login service and session id is: "+session.getId());
            	
               return userDataDto;

            }
    	    
    	} 
    	
    	catch (JSONException e) {
    		throw new TaskException(ExceptionMessages.DATA_NOT_FOUND_IN_JSON);
    	}
    	
    	catch (NumberFormatException e) {
    	    throw new TaskException( ExceptionMessages.LONG_OBJECT_CONVERSION_ERROR,e);
    	}


      
         return null; // Either user not found or wrong password
     
    }
    
    
    
    
    
    
    private void setSessionData(HttpSession session, long userId, Role role, long epochMilli) throws TaskException {     
 


   	 
    //	session.setAttribute("SessionData", sessionData);
    	
    	
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}
