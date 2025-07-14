package com.sbank.netbanking.handler.analytics;


import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sbank.netbanking.dao.AnalyticsDAO;
import com.sbank.netbanking.dto.ErrorResponse;
import com.sbank.netbanking.exceptions.TaskException;
import com.sbank.netbanking.handler.model.AccountActivity;
import com.sbank.netbanking.util.ErrorResponseUtil;
import com.sbank.netbanking.util.PojoJsonConverter;

public class AdminAnalyticsHandler {
    private final AnalyticsDAO analyticsDAO = new AnalyticsDAO();
    private final PojoJsonConverter converter = new PojoJsonConverter();

    public void getMonthlyTotals(HttpServletRequest req, HttpServletResponse res) throws TaskException {
        try {
            Map<String, Double> map = analyticsDAO.getMonthlyDepositWithdrawal();
            JSONObject json = new JSONObject(map);

            res.setContentType("application/json");
            res.getWriter().write(json.toString());
        } catch (IOException e) {
            ErrorResponseUtil.send(res, 500,
                    new ErrorResponse("Error", 500, e.getMessage()));
        }
    }

    public void getTopActiveAccounts(HttpServletRequest req, HttpServletResponse res) throws TaskException {
        int days = parseIntParam(req, "days", 30);
        int limit = Math.min(parseIntParam(req, "limit", 5), 10); // Limit to 10  

        try {
            List<AccountActivity> list = analyticsDAO.getTopActiveAccounts(limit, days);
            JSONArray arr = new JSONArray();

            for (AccountActivity activity : list) {
                arr.put(converter.pojoToJson(activity));
            }

            JSONObject result = new JSONObject();
            result.put("topActiveAccounts", arr);

            res.setContentType("application/json");
            res.getWriter().write(result.toString());
        } catch (IOException  e) {
            ErrorResponseUtil.send(res, 500,
                    new ErrorResponse("Error", 500, e.getMessage()));
        }
    }

    private int parseIntParam(HttpServletRequest req, String key, int defaultVal) {
        try {
            return Integer.parseInt(req.getParameter(key));
        } catch (Exception e) {
            return defaultVal;
        }
    }
}
