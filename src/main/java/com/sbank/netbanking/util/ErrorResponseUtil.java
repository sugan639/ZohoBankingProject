package com.sbank.netbanking.util;

import com.sbank.netbanking.dto.ErrorResponse;

import org.json.JSONObject;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ErrorResponseUtil {
    public static void send(HttpServletResponse response, int statusCode, ErrorResponse errorResponse) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json");
        JSONObject json = new JSONObject();
        json.put("error", errorResponse.getError());
        json.put("code", errorResponse.getCode());
        json.put("message", errorResponse.getMessage());
        json.put("timestamp", errorResponse.getTimestamp());
        response.getWriter().write(json.toString());
    }
}