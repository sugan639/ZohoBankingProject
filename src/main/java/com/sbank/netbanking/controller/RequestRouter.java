package com.sbank.netbanking.controller;



import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sbank.netbanking.interfaces.HandlerInterface;
import com.sbank.netbanking.routing.RouteRegistry;
import com.sbank.netbanking.util.RequestParser;

public class RequestRouter {

	
    public void route(HttpServletRequest request, HttpServletResponse response, String method) throws Exception {
       

    	RouteRegistry routeRegistry = new RouteRegistry();
    	RequestParser requestParser = new RequestParser();
    	
    	
    		
    	String fullPath = request.getPathInfo();
    
        
        
        
        if (fullPath == null || fullPath.isEmpty()) {
        	
            sendError(response, 404, "Path is Empty");
            return; 
        }

        
        	String path = requestParser.pathParser(fullPath);  // Parses the path from the URI
        	System.out.println("==============================");
            System.out.println(fullPath + " on RequestRouter");
            String uripath = request.getRequestURI();
            System.out.println("Requested URI Path: "+uripath);
            
        	System.out.println("==============================");

        	
        

        HandlerInterface handler = routeRegistry.matchRoute(method, path);
   
        if (handler != null) {
              handler.handle(request, response);
        } else {
            sendError(response, 404, "Endpoint Not Found in Registry");
        }
    }
    
   
    public void sendError(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(code);
        response.setContentType("application/json");
        response.getWriter().write(String.format("{\"error\": \"%s\", \"code\": %d}", message, code));
    }
    


}













































































































//
//import java.io.BufferedReader;
//import java.io.IOException;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import org.json.JSONObject;
//
//
//public class RequestRouter {
//
//    public void route(HttpServletRequest request, HttpServletResponse response, String method) throws IOException {
//        
//    	String path = request.getPathInfo(); // e.g., /auth/login or /customer/profile/101
//      
//
//        System.out.println("Routing: " + method + " " + path);
//        
//        
//
//        switch (path) {
//
//	        case "/auth/login":
//	            if ("POST".equals(method)) {
//	            	StringBuilder jsonBuilder = new StringBuilder();
//	                try (BufferedReader reader = request.getReader()) {
//	                    String line;
//	                    while ((line = reader.readLine()) != null) {
//	                        jsonBuilder.append(line);
//	                    }
//	                }
//
//	                // Convert to JSONObject
//	                JSONObject jsonObject = new JSONObject(jsonBuilder.toString());
//
//	                // Example: Extract data
//	                int username = jsonObject.getInt("user_id");
//	                String password = jsonObject.getString("password");
//
//	                
//	                
//	                // Respond
//	                response.setContentType("application/json");
//	                response.getWriter().write("{\"message\":\"User Authorized " + username + "\"}");
//	            }
//	            break;
//	          
////
//            default:
//                sendError(response, 404, "Endpoint Not Found");
//                break;
//        }
//    }
//
//    private void sendError(HttpServletResponse response, int code, String message) throws IOException {
//        response.setStatus(code);
//        response.setContentType("application/json");
//        response.getWriter().write(String.format("{\"error\": \"%s\", \"code\": %d}", message, code));
//    }
//}