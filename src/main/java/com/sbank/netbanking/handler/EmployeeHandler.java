package com.sbank.netbanking.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class EmployeeHandler {

    // GET /employee/profile/{user_id}
    public void getProfile(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.getWriter().write("{\"status\":\"EmployeeHandler.getProfile not implemented\"}");
    }

    // POST /employee/customers
    public void addCustomer(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.getWriter().write("{\"status\":\"EmployeeHandler.addCustomer not implemented\"}");
    }

    // GET /employee/branches/requests
    public void getBranchRequests(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.getWriter().write("{\"status\":\"EmployeeHandler.getBranchRequests not implemented\"}");
    }

    // GET /employee/accounts
    public void getBranchAccounts(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.getWriter().write("{\"status\":\"EmployeeHandler.getBranchAccounts not implemented\"}");
    }

    // GET /employee/transactions
    public void findTransactions(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.getWriter().write("{\"status\":\"EmployeeHandler.findTransactions not implemented\"}");
    }

    // GET /employee/users/{user_id}
    public void getUser(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.getWriter().write("{\"status\":\"EmployeeHandler.getUser not implemented\"}");
    }

    // PUT /employee/users/{user_id}
    public void updateUser(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.getWriter().write("{\"status\":\"EmployeeHandler.updateUser not implemented\"}");
    }

    // POST /employee/transactions/deposit
    public void deposit(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.getWriter().write("{\"status\":\"EmployeeHandler.deposit not implemented\"}");
    }

    // POST /employee/transactions/withdraw
    public void withdraw(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.getWriter().write("{\"status\":\"EmployeeHandler.withdraw not implemented\"}");
    }

    // POST /employee/transactions/transfer
    public void transfer(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.getWriter().write("{\"status\":\"EmployeeHandler.transfer not implemented\"}");
    }

    // POST /employee/account
    public void createAccount(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.getWriter().write("{\"status\":\"EmployeeHandler.createAccount not implemented\"}");
    }
}
