package com.sbank.netbanking.json;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;

public class JsonReader {
	public JSONObject readJSON(HttpServletRequest request) throws IOException {
	    StringBuilder sb = new StringBuilder();
	    String line;

	    try (BufferedReader reader = request.getReader()) {
	        while ((line = reader.readLine()) != null) {
	            sb.append(line.trim());
	        }
	    }

	    String body = sb.toString();
	    if (body.isEmpty()) {
	        throw new IOException("Request body is empty");
	    }

	    return new JSONObject(body);
	}

}
