package com.sbank.netbanking.auth;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sbank.netbanking.dto.ErrorResponse;
import com.sbank.netbanking.exceptions.TaskException;
import com.sbank.netbanking.model.SessionData;
import com.sbank.netbanking.service.SessionService;
import com.sbank.netbanking.util.CookieUtil;
import com.sbank.netbanking.util.ErrorResponseUtil;

@WebFilter("/*")
public class AuthFilter implements Filter {

    

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        SessionService sessionService = new SessionService();

        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
        System.out.println("Intercepted path: " + path);
        
        try {
            if (isPublicRoute(path)) {
                chain.doFilter(request, response);
                return;
            }

            CookieUtil cookieUtil = new CookieUtil();
            String sessionId = cookieUtil.getSessionIdFromCookies(httpRequest);
            if (sessionId == null) {
            	 ErrorResponseUtil.send(httpResponse, HttpServletResponse.SC_UNAUTHORIZED,
                         new ErrorResponse("Unauthorized", 401, "Missing session cookie"));
                     return;
            }
            
            SessionData sessionData =  new SessionData();
            sessionData =  sessionService.sessionValidator(sessionId); // Returns session data if valid session exists

            if (sessionData != null) {
            	request.setAttribute("sessionData", sessionData); // Setting sessionID as request attribute
                chain.doFilter(request, response);
            } else {
            	 ErrorResponseUtil.send(httpResponse, HttpServletResponse.SC_UNAUTHORIZED,
                         new ErrorResponse("Unauthorized", 401, "Session expired or invalid"));
            }

        } catch (TaskException e) {
        	 ErrorResponseUtil.send(httpResponse, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                     new ErrorResponse("TaskException", 500, e.getMessage()));
        }
    }

    private boolean isPublicRoute(String path) {
        return path.startsWith("/auth/login") ||
               path.startsWith("/auth/logout") ||
               path.startsWith("/auth/register");
    }

   
}
