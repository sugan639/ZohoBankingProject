package com.sbank.netbanking.handler.analytics;


import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sbank.netbanking.dao.AnalyticsDAO;
import com.sbank.netbanking.dao.EmployeeDAO;
import com.sbank.netbanking.dto.ErrorResponse;
import com.sbank.netbanking.exceptions.TaskException;
import com.sbank.netbanking.model.BranchCustomerBalance;
import com.sbank.netbanking.model.Employee;
import com.sbank.netbanking.model.SessionData;
import com.sbank.netbanking.util.ErrorResponseUtil;
import com.sbank.netbanking.util.PojoJsonConverter;

public class EmployeeAnalyticsHandler {

    private final AnalyticsDAO analyticsdDao = new AnalyticsDAO();
    private final EmployeeDAO employeeDAO      = new EmployeeDAO();
    private final PojoJsonConverter converter  = new PojoJsonConverter();

    /* GET /employee/analytics/summary?scope=today|month */
    public void branchSummary(HttpServletRequest req, HttpServletResponse res) throws TaskException {
        try {
            // Session
            SessionData sd = (SessionData) req.getAttribute("sessionData");
            Employee emp   = employeeDAO.getEmployeeById(sd.getUserId());
            long branchId  = emp.getBranchId();

            String scope = Optional.ofNullable(req.getParameter("scope")).orElse("today");
            long from, to = System.currentTimeMillis();

            if ("month".equalsIgnoreCase(scope)) {
                from = analyticsdDao.startOfMonth();
            } else {
                from = analyticsdDao.startOfToday();
            }

            Map<String, Double> map = analyticsdDao.getBranchTxnSummary(branchId, from, to);

            JSONObject out = new JSONObject(map);
            out.put("branchId", branchId);
            out.put("scope", scope);

            res.setContentType("application/json");
            res.getWriter().write(out.toString());

        } catch (IOException e) {
            ErrorResponseUtil.send(res, 500, new ErrorResponse("Error", 500, e.getMessage()));
        }
    }

    /* GET /employee/analytics/top-customers?limit=5 */
    public void topCustomers(HttpServletRequest req, HttpServletResponse res) throws TaskException {
        int limit =  Math.min(parseInt(req.getParameter("limit"), 5), 10);

        try {
            SessionData sd = (SessionData) req.getAttribute("sessionData");
            Employee emp   = employeeDAO.getEmployeeById(sd.getUserId());
            long branchId  = emp.getBranchId();

            List<BranchCustomerBalance> list = analyticsdDao.getTopCustomers(branchId, limit);

            JSONArray arr = new JSONArray();
            for (BranchCustomerBalance b : list) {
                arr.put(converter.pojoToJson(b));
            }

            JSONObject result = new JSONObject();
            result.put("branchId", branchId);
            result.put("topCustomers", arr);

            res.setContentType("application/json");
            res.getWriter().write(result.toString());
        } catch (IOException e) {
            ErrorResponseUtil.send(res, 500, new ErrorResponse("Error", 500, e.getMessage()));
        }
    }

    /* simple parse util */
    private int parseInt(String val, int def) {
        try { 
        	return Integer.parseInt(val);
        	} 
        catch (Exception e) {
        	return def;
        	}
    }
}




























