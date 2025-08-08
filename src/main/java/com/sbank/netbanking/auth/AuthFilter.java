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

import com.sbank.auth.request.validatot.RequestValidator;
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

        // --- CORS Setup ---
        String origin = httpRequest.getHeader("Origin");
        if (origin != null && (origin.equals("http://localhost:3000") || origin.equals("http://localhost:3001"))) {
            httpResponse.setHeader("Access-Control-Allow-Origin", origin);
        }

        httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        httpResponse.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");

        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());

        

        try {
            // --- Public Route Check ---
            if (isPublicRoute(path)) {
                chain.doFilter(request, response);
                return;
            }
            
            

            // --- Session Handling ---
            CookieUtil cookieUtil = new CookieUtil();
            String[] values = cookieUtil.getSessionIdAndUserId(httpRequest);
           
            
            if (values == null) {
                ErrorResponseUtil.send(httpResponse, HttpServletResponse.SC_UNAUTHORIZED,
                        new ErrorResponse("Unauthorized", 401, "Missing session cookie"));
                return;
            }
            
            String sessionUUID = values[0];	// UUID
            String sessionId = values[1];	// SessionId
            
            System.out.println("ID: "+sessionId);
            
            
            SessionData sessionData = sessionService.sessionValidator(sessionUUID, sessionId);
            if (sessionData == null) {
                ErrorResponseUtil.send(httpResponse, HttpServletResponse.SC_UNAUTHORIZED,
                        new ErrorResponse("Unauthorized", 401, "Session expired or invalid"));
                return;
            }

            // --- Request Field Validation ---
            StringBuilder error = new StringBuilder();
            if (RequestValidator.isValid(httpRequest, error)) {
                httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                httpResponse.getWriter().write("{\"error\": \"" + error.toString() + "\"}");
                return;
            }

            // --- Proceed to next layer ---
            request.setAttribute("sessionData", sessionData);
            chain.doFilter(request, response);

        } catch (TaskException e) {
            try {
                ErrorResponseUtil.send(httpResponse, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        new ErrorResponse("TaskException", 500, e.getMessage()));
            } catch (TaskException ex) {
                ex.printStackTrace(); // Should be logged properly
            }
        }
    }

    private boolean isPublicRoute(String path) {
        return path.startsWith("/auth/login") ||
               path.startsWith("/auth/logout") ||
               path.startsWith("/register");
    }
}
