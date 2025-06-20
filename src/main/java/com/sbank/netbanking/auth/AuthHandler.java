package com.sbank.netbanking.auth;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.sbank.netbanking.controller.RequestRouter;
import com.sbank.netbanking.dto.UserDTO;
import com.sbank.netbanking.exceptions.ExceptionMessages;
import com.sbank.netbanking.exceptions.TaskException;
import com.sbank.netbanking.service.LoginService;
import com.sbank.netbanking.service.SessionService;
import com.sbank.netbanking.util.CookieUtil;
import com.sbank.netbanking.util.PojoJsonConverter;
import com.sbank.netbanking.util.RequestJsonConverter;

public class AuthHandler {
	
	
	//Login method
	public void login(HttpServletRequest req, HttpServletResponse res) throws TaskException {
		
		LoginService loginService =  new LoginService();
		UserDTO userDataDto = new UserDTO();
		PojoJsonConverter pojoToJson = new PojoJsonConverter();
		RequestJsonConverter reader =  new RequestJsonConverter();

		System.out.println("Request reached login method");
		
		// Request to json formating
	    JSONObject json = reader.convertToJson(req);  

        // Returns user DTO object if credentials are correct else null
        userDataDto = loginService.validator(json, req,res); 

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
	public void logout(HttpServletRequest req, HttpServletResponse res) throws TaskException {
		
        CookieUtil cookieUtil = new CookieUtil();
        String sessionId = cookieUtil.getSessionIdFromCookies(req);

 	    SessionService sessionService = new SessionService();

 	    if (sessionId != null) {
	      
	        sessionService.deleteDbCookies(sessionId);
	        // Clear the cookie from browser
	        Cookie cookie = new Cookie("BANK_SESSION_ID", "");
	        cookie.setHttpOnly(true);
	        cookie.setSecure(true); 
	        cookie.setPath("/");
	        cookie.setMaxAge(0); // Tells browser to delete cookies immediately
	        res.addCookie(cookie);
	      
	    }

	    try {
	    JSONObject responseJson = new JSONObject();
	    responseJson.put("status", "success");
	    responseJson.put("message", "Logged out successfully");
	    res.setContentType("application/json");
	    res.getWriter().write(responseJson.toString());
	    
	    }
	    
	    catch(IOException e) {
        	throw new TaskException(ExceptionMessages.RESPONSE_WRITER_FAILED);
        }
	    
	}
}




















