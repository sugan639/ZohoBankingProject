package com.sbank.netbanking.auth;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.sbank.netbanking.exceptions.TaskException;
import com.sbank.netbanking.service.SessionService;

@WebFilter("/*")
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
       
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException
            {
    	try {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        SessionService sessionService = new SessionService();

        HttpSession session = httpRequest.getSession(false); // Don't create new session if there is no session present

        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());

        System.out.println("Intercepted path: " + path);
        System.out.println("Session ID: " + (session != null ? session.getId() : "No session"));

        // Skip authentication for login/logout
        if (isPublicRoute(path) && session == null) {		// Allow all for testing
            chain.doFilter(request, response);
            return;
        }
        
        if(session==null) {
        	System.out.println("Null session");
        }

       
        
        // Check if user is authenticated
        if (session != null && sessionService.sessionValidator(session)) {
            // User is logged in
            chain.doFilter(request, response);
        } else {
            // Unauthorized access
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"Login required\"}");
        }
    }
    	
    	
    	catch(TaskException | IOException e) {
    		
    	        response.setContentType("application/json");
    	        response.getWriter().write(String.format("{\"error\": \"%s}", e));
    	
    	
            }
            }
    
    

    private boolean isPublicRoute(String path) {
        return path.equals("/auth/login") || path.equals("/auth/logout") || path.equals("/auth/register");
    }
    
    
    

    @Override
    public void destroy() {
  
    }
}