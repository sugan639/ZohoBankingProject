package com.sbank.netbanking.service;

import org.json.JSONException;
import org.json.JSONObject;

import com.sbank.netbanking.dao.UserDAO;
import com.sbank.netbanking.dto.UserDTO;
import com.sbank.netbanking.exceptions.ExceptionMessages;
import com.sbank.netbanking.exceptions.TaskException;
import com.sbank.netbanking.model.User;
import com.sbank.netbanking.util.UserMapper;

public class LoginService {

    public UserDTO validator(JSONObject json) throws  TaskException {

    	
    	try {
    		String userIdString = json.getString("user_id");
            String passwordInput = json.getString("password");
            
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
}
