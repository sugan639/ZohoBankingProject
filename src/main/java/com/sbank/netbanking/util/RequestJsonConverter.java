package com.sbank.netbanking.util;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;

import com.sbank.netbanking.exceptions.ExceptionMessages;
import com.sbank.netbanking.exceptions.TaskException;
import com.sbank.netbanking.json.JsonReader;

public class RequestJsonConverter {
	public JSONObject convertToJson(HttpServletRequest req) throws  TaskException {
		try {
		JsonReader reader =  new JsonReader();
		JSONObject json = reader.readJSON(req);
		return json;
		
		}
		catch(IOException e) {
			throw new TaskException(ExceptionMessages.REQUEST_PARSE_TO_JSON_FAILED,e);
		}
	
	}

}
