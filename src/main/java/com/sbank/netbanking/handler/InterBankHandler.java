package com.sbank.netbanking.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.sbank.netbanking.exceptions.TaskException;
import com.sbank.netbanking.util.RequestJsonConverter;

public class InterBankHandler {

	public void interbankCredit(HttpServletRequest req, HttpServletResponse res) throws TaskException {
		RequestJsonConverter jsonConverter = new RequestJsonConverter();

		JSONObject json = jsonConverter.convertToJson(req);

	}
}
