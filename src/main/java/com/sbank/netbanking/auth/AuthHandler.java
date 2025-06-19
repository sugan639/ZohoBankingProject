package com.sbank.netbanking.auth;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONObject;

import com.sbank.netbanking.controller.RequestRouter;
import com.sbank.netbanking.dao.SessionDAO;
import com.sbank.netbanking.dto.UserDTO;
import com.sbank.netbanking.exceptions.ExceptionMessages;
import com.sbank.netbanking.exceptions.TaskException;
import com.sbank.netbanking.model.SessionData;
import com.sbank.netbanking.model.User.Role;
import com.sbank.netbanking.service.LoginService;
import com.sbank.netbanking.service.SessionService;
import com.sbank.netbanking.util.PojoJsonConverter;
import com.sbank.netbanking.util.RequestJsonConverter;

public class AuthHandler {
	
	
	//Login method
	public void login(HttpServletRequest req, HttpServletResponse res) throws TaskException {
		
		LoginService loginService =  new LoginService();
		UserDTO userDataDto = new UserDTO();
		PojoJsonConverter pojoToJson = new PojoJsonConverter();
		RequestJsonConverter reader =  new RequestJsonConverter();
		SessionService sessionService = new SessionService();
		HttpSession session = req.getSession(); 
		// Request to json formating
	    JSONObject json = reader.convertToJson(req); 
		String userIdString = json.getString("user_id");
        String passwordInput = json.getString("password");
        Role role = Role.valueOf(json.getString("role"));
        long epochMilli = Instant.now().toEpochMilli();
	    Long userId = Long.parseLong(userIdString);


		
		System.out.println("Request reached login method");
		
 

        // Returns user DTO object if credentials are correct else null
        userDataDto = loginService.validator(json, req); 
        
		SessionData sessionData = new SessionData(); // Session Data container
		SessionDAO sessionDao = new SessionDAO();

    	sessionData.setUserId(userId);
    	sessionData.setSessionID(session.getId());
    	sessionData.setRole(role);
    	sessionData.setStartTime(epochMilli);
    	// Have to set the sesson data object 
    	
    	sessionDao.createDbSession(sessionData);
    	session.setAttribute("SessionData", sessionData);
    	
        
	    
        try { 
        	
	    if(userDataDto!=null) { 
	
	    	
		   JSONObject jsonResponse = pojoToJson.pojoToJson(userDataDto);
		   res.setContentType("application/json");
		   PrintWriter out = res.getWriter();
		   out.print(jsonResponse);
	    }
	    
	    else {
	    	RequestRouter requestRouter = new RequestRouter();
	    	requestRouter.sendError(res, 404, "Credentials are wrong!");
	    }
     }
        
        catch(IOException e) {
        	throw new TaskException(ExceptionMessages.RESPONSE_WRITER_FAILED);
        }
	    
	    
	}

	// Logout method
	public void logout(HttpServletRequest req, HttpServletResponse res) throws IOException {
	    HttpSession session = req.getSession(false);
	    if (session != null) {
	        session.invalidate(); // End session
	    }

	    JSONObject responseJson = new JSONObject();
	    responseJson.put("status", "success");
	    responseJson.put("message", "Logged out successfully");
	    res.setContentType("application/json");
	    res.getWriter().write(responseJson.toString());
	}
}