package com.sbank.netbanking.routing;

import java.util.ArrayList;
import java.util.List;

import com.sbank.netbanking.auth.AuthHandler;
import com.sbank.netbanking.handler.AdminHandler;
import com.sbank.netbanking.handler.CustomerHandler;
import com.sbank.netbanking.handler.EmployeeHandler;
import com.sbank.netbanking.interfaces.HandlerInterface;


public class RouteRegistry {
    
	private final List<Route> routes = new ArrayList<>();

    AuthHandler authHandler = new AuthHandler();
    CustomerHandler customerHandler = new CustomerHandler();
    EmployeeHandler employeeHandler = new EmployeeHandler();
    AdminHandler adminHandler = new AdminHandler();
    
    public RouteRegistry() {
        // Authentication
        routes.add(new Route("POST", "/auth/login", authHandler::login));
        routes.add(new Route("POST", "/auth/logout", authHandler::logout));

        // Customer
        routes.add(new Route("GET", "/customer/profile", customerHandler::getProfile));
        routes.add(new Route("PUT", "/customer/profile", customerHandler::updateProfile));
        routes.add(new Route("GET", "/customer/accounts", customerHandler::getAccounts));
        routes.add(new Route("GET", "/customer/transactions", customerHandler::getTransaction));
        routes.add(new Route("POST", "/customer/transfer", customerHandler::transferMoney));
        routes.add(new Route("POST", "/customer/beneficiaries", customerHandler::addBeneficiary));

        // Employee
        routes.add(new Route("GET", "/employee/profile", employeeHandler::getProfile));
        routes.add(new Route("POST", "/employee/customers", employeeHandler::addCustomer));
        routes.add(new Route("GET", "/employee/branches/requests", employeeHandler::getBranchRequests));
        routes.add(new Route("GET", "/employee/accounts", employeeHandler::getBranchAccounts));
        routes.add(new Route("GET", "/employee/transactions", employeeHandler::findTransactions));
        routes.add(new Route("GET", "/employee/users", employeeHandler::getUser));
        routes.add(new Route("PUT", "/employee/users", employeeHandler::updateUser));
        routes.add(new Route("POST", "/employee/transactions/deposit", employeeHandler::deposit));
        routes.add(new Route("POST", "/employee/transactions/withdraw", employeeHandler::withdraw));
        routes.add(new Route("POST", "/employee/transactions/transfer", employeeHandler::transfer));
        routes.add(new Route("POST", "/employee/newAccount", employeeHandler::createAccount));

        // Admin
        routes.add(new Route("GET", "/admin/profile", adminHandler::getProfile));
        routes.add(new Route("GET", "/admin/branches", adminHandler::getBranchById));
        routes.add(new Route("PUT", "/admin/branches", adminHandler::updateBranch));
        routes.add(new Route("GET", "/admin/branches/requests", adminHandler::getBranchRequests));
        routes.add(new Route("GET", "/admin/users", adminHandler::getUser));
        routes.add(new Route("PUT", "/admin/users", adminHandler::updateUser));
        routes.add(new Route("POST", "/admin/transactions/deposit", adminHandler::deposit));
        routes.add(new Route("POST", "/admin/transactions/withdraw", adminHandler::withdraw));
        routes.add(new Route("POST", "/admin/transactions/transfer", adminHandler::transfer));
        routes.add(new Route("POST", "/admin/newAccount", adminHandler::createAccount));
        routes.add(new Route("POST", "/admin/employee", adminHandler::addEmployee));
        
        
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