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

import com.sbank.netbanking.exceptions.TaskException;
import com.sbank.netbanking.service.SessionService;
import com.sbank.netbanking.util.CookieUtil;

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
                respondUnauthorized(httpResponse, "Missing session cookie");
                return;
            }

            if (sessionService.sessionValidator(sessionId)) {
                chain.doFilter(request, response);
            } else {
                respondUnauthorized(httpResponse, "Session expired or invalid");
            }

        } catch (TaskException e) {
            httpResponse.setContentType("application/json");
            httpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            httpResponse.getWriter().write(String.format("{\"error\": \"TaskException\", \"message\": \"%s\"}", e.getMessage()));
        }
    }

    private boolean isPublicRoute(String path) {
        return path.startsWith("/auth/login") ||
               path.startsWith("/auth/logout") ||
               path.startsWith("/auth/register");
    }



    private void respondUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(String.format("{\"error\": \"Unauthorized\", \"message\": \"%s\"}", message));
    }
}
