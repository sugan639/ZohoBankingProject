package com.sbank.netbanking.routing;

import java.util.ArrayList;
import java.util.List;

import com.sbank.netbanking.auth.AuthHandler;
import com.sbank.netbanking.handler.AdminHandler;
import com.sbank.netbanking.handler.CustomerHandler;
import com.sbank.netbanking.handler.EmployeeHandler;
import com.sbank.netbanking.handler.InterBankHandler;
import com.sbank.netbanking.handler.NewUserRegister;
import com.sbank.netbanking.handler.analytics.AdminAnalyticsHandler;
import com.sbank.netbanking.handler.analytics.EmployeeAnalyticsHandler;
import com.sbank.netbanking.interfaces.HandlerInterface;
import com.sbank.netbanking.service.SessionService;


public class RouteRegistry {
    
	private final List<Route> routes = new ArrayList<>();

    AuthHandler authHandler = new AuthHandler();
    CustomerHandler customerHandler = new CustomerHandler();
    EmployeeHandler employeeHandler = new EmployeeHandler();
    AdminHandler adminHandler = new AdminHandler();
    NewUserRegister  newUserRegister = new NewUserRegister();
    AdminAnalyticsHandler analyticsHandler = new AdminAnalyticsHandler();
    EmployeeAnalyticsHandler employeeAnalyticsHandler = new EmployeeAnalyticsHandler();
    SessionService sessionService = new SessionService();
    InterBankHandler interBankHandler = new InterBankHandler();
    
    public RouteRegistry() {
        // Authentication
        routes.add(new Route("POST", "/auth/login", authHandler::login));
        routes.add(new Route("POST", "/auth/logout", authHandler::logout));
        routes.add(new Route("POST", "/register", newUserRegister::registerUser));
        routes.add(new Route("POST", "/auth/config", sessionService::clearAllUserSession));

        

        // Customer
        routes.add(new Route("GET", "/customer/profile", customerHandler::getProfile));
        routes.add(new Route("PUT", "/customer/profile/update", customerHandler::updateProfile));
        routes.add(new Route("GET", "/customer/accounts", customerHandler::getAccounts));
        routes.add(new Route("POST", "/customer/transactions", customerHandler::getTransaction));
        routes.add(new Route("POST", "/customer/transfer", customerHandler::transferMoney));
        routes.add(new Route("POST", "/customer/beneficiaries/add", customerHandler::addBeneficiary));
        routes.add(new Route("GET", "/customer/beneficiaries/get", customerHandler::getBeneficiary));

        // Employee
        routes.add(new Route("GET", "/employee/profile", employeeHandler::getProfile));
        routes.add(new Route("PUT", "/employee/profile/update", employeeHandler::updateProfile));
        routes.add(new Route("POST", "/employee/customers/create", employeeHandler::addCustomer));
        routes.add(new Route("POST", "/employee/account/update", employeeHandler::editBranchAccounts));
        routes.add(new Route("POST", "/employee/transactions/query", employeeHandler::getTransactions));
        routes.add(new Route("GET", "/employee/users", employeeHandler::getUser));
        routes.add(new Route("POST", "/employee/users/update", employeeHandler::updateCustomer));
        routes.add(new Route("POST", "/employee/transactions/deposit", employeeHandler::deposit));
        routes.add(new Route("POST", "/employee/transactions/withdraw", employeeHandler::withdraw));
        routes.add(new Route("POST", "/employee/transactions/transfer", employeeHandler::transfer));
        routes.add(new Route("POST", "/employee/new-account", employeeHandler::createAccount));
        routes.add(new Route("GET", "/employee/account/get-accounts", employeeHandler::getAccountDetails));


        
        // Admin
        routes.add(new Route("GET", "/admin/profile", adminHandler::getProfile));
        routes.add(new Route("GET", "/admin/branches/branch-id", adminHandler::getBranchById));
        routes.add(new Route("GET", "/admin/branches/ifsc-code", adminHandler::getBranchByIfsc));
        routes.add(new Route("PUT", "/admin/branches", adminHandler::updateBranch));
        routes.add(new Route("GET", "/admin/users", adminHandler::getUser));
        routes.add(new Route("POST", "/admin/users/update", adminHandler::updateUser));
        routes.add(new Route("POST", "/admin/transactions/deposit", adminHandler::deposit));
        routes.add(new Route("POST", "/admin/transactions/withdraw", adminHandler::withdraw));
        routes.add(new Route("POST", "/admin/transactions/transfer", adminHandler::transfer));
        routes.add(new Route("POST", "/admin/customer/new-account", adminHandler::createAccount));
        routes.add(new Route("POST", "/admin/account/update", adminHandler::updateAccountStatus));
        routes.add(new Route("POST", "/admin/new-employee", adminHandler::addEmployee));
        routes.add(new Route("POST", "/admin/new-customer", adminHandler::addCustomer));
        routes.add(new Route("POST", "/admin/transactions/query", adminHandler::queryTransactions));
        routes.add(new Route("POST", "/admin/branch/create", adminHandler::createBranch));
        routes.add(new Route("GET", "/admin/account/get-accounts", adminHandler::getAccountDetails));
        routes.add(new Route("POST", "/admin/new-client-bank", adminHandler::addClientBank));


        // Analytics
        routes.add(new Route("GET", "/admin/analytics/monthly-totals", analyticsHandler::getMonthlyTotals));
        routes.add(new Route("GET", "/admin/analytics/top-accounts",   analyticsHandler::getTopActiveAccounts));
        routes.add(new Route("GET", "/employee/analytics/transaction-summary",      employeeAnalyticsHandler::branchSummary));
        routes.add(new Route("GET", "/employee/analytics/top-customers", employeeAnalyticsHandler::topCustomers));
        routes.add(new Route("GET", "/customer/analytics/dashboard-summary", customerHandler::analyticSummary));
        
        
        // Interbank API 
        
        routes.add(new Route("POST", "/interbank/transfer", interBankHandler::interbankCredit));
        

        

    }
    
    

    public HandlerInterface matchRoute(String method, String path) {
        for (Route route : routes) {
            if (route.matches(method, path)) {
                return route.getHandler();
            }
        }
        return null;
    }

    
    
    
}