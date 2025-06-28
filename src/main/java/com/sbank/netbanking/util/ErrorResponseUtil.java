package com.sbank.netbanking.util;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.sbank.netbanking.dto.ErrorResponse;
import com.sbank.netbanking.exceptions.TaskException;

public class ErrorResponseUtil {
    public static void send(HttpServletResponse response, int statusCode, ErrorResponse errorResponse) throws  TaskException {
        
    	try {
    	response.setStatus(statusCode);
        response.setContentType("application/json");
        JSONObject json = new JSONObject();
        json.put("error", errorResponse.getError());
        json.put("code", errorResponse.getCode());
        json.put("message", errorResponse.getMessage());
        json.put("timestamp", errorResponse.getTimestamp());
        response.getWriter().write(json.toString());
    	}
    	catch(IOException e) {
    		throw new TaskException("Problem in error response utility method.");
    	}
    }
}