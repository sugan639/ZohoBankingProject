package com.sbank.netbanking.handler;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.sbank.netbanking.dao.AdminDAO;
import com.sbank.netbanking.dao.BranchDAO;
import com.sbank.netbanking.exceptions.TaskException;
import com.sbank.netbanking.model.Admin;
import com.sbank.netbanking.model.Branch;
import com.sbank.netbanking.model.SessionData;
import com.sbank.netbanking.util.PojoJsonConverter;

public class AdminHandler {


	public void getProfile(HttpServletRequest req, HttpServletResponse res) throws IOException, TaskException {

	    SessionData sessionData =  new SessionData();
	    sessionData = (SessionData) req.getAttribute("sessionData");
        long adminId = sessionData.getUserId();
      
        System.out.println("User ID from the session data get profile method: "+adminId);
        AdminDAO adminDAO = new AdminDAO();
        PojoJsonConverter converter = new PojoJsonConverter();

        try {
            Admin admin = adminDAO.getAdminById(adminId);
            if (admin != null) {
                JSONObject jsonAdmin = converter.pojoToJson(admin);
                res.setContentType("application/json");
                res.getWriter().write(jsonAdmin.toString());
            } else {
                res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                res.getWriter().write("{\"error\":\"Admin not found\", \"code\":404}");
            }
        } catch (TaskException e) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            res.getWriter().write(String.format("{\"error\": \"%s\"}", e.getMessage()));
        }
      }
    

	// GET /admin/branches/{branch_id}
	public void getBranchById(HttpServletRequest req, HttpServletResponse res) throws TaskException {
	    try {
	    	
	        String branchIdStr = req.getParameter("branchId");

	        if (branchIdStr == null || branchIdStr.isEmpty()) {
	            throw new TaskException("Missing or invalid 'branchId' in query parameters.");
	        }

	        long branchId;
	        try {
	            branchId = Long.parseLong(branchIdStr);
	        } catch (NumberFormatException e) {
	            throw new TaskException("Invalid branch ID format. Must be a number.", e);
	        }

	       


	        Branch branch = new Branch();
	        BranchDAO branchDao = new BranchDAO();

	        
	        branch = branchDao.getBranchById(branchId);  // Branch POJO
	        System.out.println("Branch ID from the GET request data @ getBranch method: "+branchId);

	        PojoJsonConverter converter = new PojoJsonConverter();
	        JSONObject jsonBranch = converter.pojoToJson(branch); // Branch JSON

	       
	        res.setContentType("application/json");
	        res.getWriter().write(jsonBranch.toString());

	    } catch (NumberFormatException e) {
	        throw new TaskException("Invalid branch ID format", e);
	    } catch (IOException e) {
	        throw new TaskException("Failed to write branch response", e);
	    }
	}


    // PUT /admin/branches/{branch_id}
    public void updateBranch(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.getWriter().write("{\"status\":\"AdminHandler.updateBranch not implemented\"}");
    }

    // GET /admin/users/{user_id}
    public  void getUser(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.getWriter().write("{\"status\":\"AdminHandler.getUser not implemented\"}");
    }

    // PUT /admin/users/{user_id}
    public  void updateUser(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.getWriter().write("{\"status\":\"AdminHandler.updateUser not implemented\"}");
    }

    // POST /admin/transactions/deposit
    public  void deposit(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.getWriter().write("{\"status\":\"AdminHandler.deposit not implemented\"}");
    }

    // POST /admin/transactions/withdraw
    public void withdraw(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.getWriter().write("{\"status\":\"AdminHandler.withdraw not implemented\"}");
    }

    // POST /admin/transactions/transfer
    public void transfer(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.getWriter().write("{\"status\":\"AdminHandler.transfer not implemented\"}");
    }

    // POST /admin/account
    public void createAccount(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.getWriter().write("{\"status\":\"AdminHandler.createAccount not implemented\"}");
    }

    // POST /admin/employee
    public void addEmployee(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.getWriter().write("{\"status\":\"AdminHandler.addEmployee not implemented\"}");
    }
}