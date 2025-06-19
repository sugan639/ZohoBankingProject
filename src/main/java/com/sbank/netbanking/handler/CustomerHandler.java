package com.sbank.netbanking.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CustomerHandler {
    public void getProfile(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.getWriter().write("{\"status\":\"CustomerHandler.getProfile not implemented\"}");
    }

    public void updateProfile(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.getWriter().write("{\"status\":\"CustomerHandler.updateProfile not implemented\"}");
    }

    public void getAccounts(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.getWriter().write("{\"status\":\"CustomerHandler.getAccounts not implemented\"}");
    }

    public void transferMoney(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.getWriter().write("{\"status\":\"CustomerHandler.transferMoney not implemented\"}");
    }

    public void addBeneficiary(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.getWriter().write("{\"status\":\"CustomerHandler.addBeneficiary not implemented\"}");
    }

    public void getTransaction(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.getWriter().write("{\"status\":\"CustomerHandler.getTransaction not implemented\"}");
    }
}