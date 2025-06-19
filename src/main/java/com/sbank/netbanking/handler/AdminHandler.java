package com.sbank.netbanking.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AdminHandler {

    // GET /admin/profile/{user_id}
    public void getProfile(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.getWriter().write("{\"status\":\"AdminHandler.getProfile not implemented\"}");
    }

    // GET /admin/branches/{branch_id}
    public void getBranchById(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.getWriter().write("{\"status\":\"AdminHandler.getBranchById not implemented\"}");
    }

    // PUT /admin/branches/{branch_id}
    public void updateBranch(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.getWriter().write("{\"status\":\"AdminHandler.updateBranch not implemented\"}");
    }

    // GET /admin/branches/requests
    public void getBranchRequests(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.getWriter().write("{\"status\":\"AdminHandler.getBranchRequests not implemented\"}");
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