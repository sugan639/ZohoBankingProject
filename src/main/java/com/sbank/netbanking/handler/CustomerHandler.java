package com.sbank.netbanking.handler;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sbank.netbanking.dao.AccountDAO;
import com.sbank.netbanking.dao.CustomerDAO;
import com.sbank.netbanking.dao.TransactionDAO;
import com.sbank.netbanking.dao.TransactionUtil;
import com.sbank.netbanking.dao.UserDAO;
import com.sbank.netbanking.dto.ErrorResponse;
import com.sbank.netbanking.exceptions.TaskException;
import com.sbank.netbanking.model.Account;
import com.sbank.netbanking.model.Account.AccountStatus;
import com.sbank.netbanking.model.Beneficiary;
import com.sbank.netbanking.model.Customer;
import com.sbank.netbanking.model.SessionData;
import com.sbank.netbanking.model.Transaction;
import com.sbank.netbanking.model.Transaction.TransactionType;
import com.sbank.netbanking.util.DateUtil;
import com.sbank.netbanking.util.ErrorResponseUtil;
import com.sbank.netbanking.util.PojoJsonConverter;
import com.sbank.netbanking.util.RequestJsonConverter;

public class CustomerHandler {
    public void getProfile(HttpServletRequest req, HttpServletResponse res) throws TaskException {

    	SessionData sessionData = (SessionData) req.getAttribute("sessionData");

        long customerId = sessionData.getUserId();
      
        System.out.println("User ID from the session data get profile method: "+customerId);
        CustomerDAO customerDAO = new CustomerDAO();
        PojoJsonConverter converter = new PojoJsonConverter();

        try {
        	Customer customer = customerDAO.getCustomerById(customerId);
            if (customer != null) {
                JSONObject jsonAdmin = converter.pojoToJson(customer);
                res.setContentType("application/json");
                res.getWriter().write(jsonAdmin.toString());
            } else {
                res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                res.getWriter().write("{\"error\":\"Customer not found\", \"code\":404}");
            }
        } catch (IOException e) {
        	  ErrorResponseUtil.send(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
      	            new ErrorResponse("TaskException", 500, e.getMessage()));
        }
    }

 // PUT /customer/profile
    public void updateProfile(HttpServletRequest req, HttpServletResponse res) throws TaskException {
        RequestJsonConverter jsonConverter = new RequestJsonConverter();
        UserDAO userDAO = new UserDAO();
        CustomerDAO customerDAO = new CustomerDAO();

        try {
            JSONObject json = jsonConverter.convertToJson(req);

            // Get session info
            SessionData sessionData = (SessionData) req.getAttribute("sessionData");
            long userId = sessionData.getUserId();

            // Extract editable fields
            String name = json.optString("name", null);
            String email = json.optString("email", null);
            Long mobileNumber = json.has("mobile_number") ? json.getLong("mobile_number") : null;
            String dobStr = json.optString("dob", null);
            Long dob = dobStr != null ? DateUtil.convertDateToEpoch(dobStr) : null;
            String address = json.optString("address", null);

            // Update users table
            userDAO.updateUserFields(userId, name, email, mobileNumber, userId);

            // Update customers table (DOB & address only)
            customerDAO.updateCustomerProfileFields(userId, dob, address);

            JSONObject response = new JSONObject();
            response.put("message", "Profile updated successfully");
            res.setContentType("application/json");
            res.getWriter().write(response.toString());

        } catch (IOException e) {
            e.printStackTrace();
            ErrorResponseUtil.send(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                new ErrorResponse("TaskException", 500, e.getMessage()));
        }
    }


    public void getAccounts(HttpServletRequest req, HttpServletResponse res) throws TaskException {
        AccountDAO accountDAO = new AccountDAO();
        PojoJsonConverter converter = new PojoJsonConverter();

        try {
            SessionData sessionData = (SessionData) req.getAttribute("sessionData");
            long userId = sessionData.getUserId();

            List<Account> accounts = accountDAO.getAccountsByUserId(userId);

            JSONArray jsonAccounts = new JSONArray();
            for (Account acc : accounts) {
                jsonAccounts.put(converter.pojoToJson(acc));
            }

            JSONObject response = new JSONObject();
            response.put("accounts", jsonAccounts);
            res.setContentType("application/json");
            res.getWriter().write(response.toString());

        } catch (IOException e) {
            e.printStackTrace();
            ErrorResponseUtil.send(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                new ErrorResponse("TaskException", 500, e.getMessage()));
        }
    }

    public void transferMoney(HttpServletRequest req, HttpServletResponse res) throws TaskException {
        RequestJsonConverter jsonConverter = new RequestJsonConverter();
        PojoJsonConverter pojoConverter = new PojoJsonConverter();
        TransactionDAO transactionDAO = new TransactionDAO();
        TransactionUtil transactionUtil = new TransactionUtil();

        try {
            JSONObject json = jsonConverter.convertToJson(req);

            Long fromAccount = json.has("from_account") ? json.getLong("from_account") : null;
            Long toAccount = json.has("to_account") ? json.getLong("to_account") : null;
            Double amount = json.has("amount") ? json.getDouble("amount") : null;

            if (fromAccount == null || toAccount == null || amount == null || amount <= 0) {
                ErrorResponseUtil.send(res, HttpServletResponse.SC_BAD_REQUEST,
                    new ErrorResponse("Bad Request", 400, "from_account, to_account and valid amount are required"));
                return;
            }

            // Get session user
            SessionData sessionData = (SessionData) req.getAttribute("sessionData");
            long userId = sessionData.getUserId();
            System.out.println("Customer user ID: "+userId);

            // Validate fromAccount belongs to this customer
            AccountDAO accountDAO = new AccountDAO();
            Account acc = accountDAO.getAccountByNumber(fromAccount);
            System.out.println("Customer account number: "+fromAccount);
            if (acc == null || acc.getUserId() != userId) {
                ErrorResponseUtil.send(res, HttpServletResponse.SC_UNAUTHORIZED,
                    new ErrorResponse("Unauthorized", 403, "Cannot transfer from another user's account"));
                return;
            }
            
            
            String status = acc.getStatus().name();
            if (!AccountStatus.ACTIVE.name().equalsIgnoreCase(status)) {
                ErrorResponseUtil.send(res, HttpServletResponse.SC_BAD_REQUEST,
                    new ErrorResponse("Bad Request", 400, "Your source account is not ACTIVE"));
                return;
            }

            // Generate transaction ID shared for both entries
            long transactionId = transactionUtil.generateTransactionId();

            // 1. Withdraw from sender
            Transaction debitTxn = transactionDAO.withdraw(fromAccount, amount, userId,
                TransactionType.INTRA_BANK_DEBIT, transactionId, toAccount, null);

            if (debitTxn.getStatus() == Transaction.TransactionStatus.FAILED) {
                // If withdrawal failed, skip deposit
                JSONObject resp = pojoConverter.pojoToJson(debitTxn);
                resp.put("message", "Insufficient balance. Transfer failed.");
                res.setContentType("application/json");
                res.getWriter().write(resp.toString());
                return;
            }

            // 2. Deposit to recipient
            Transaction creditTxn = transactionDAO.deposit(toAccount, amount, userId,
                TransactionType.INTRA_BANK_CREDIT, transactionId, fromAccount, null);

            // Success response
            JSONObject response = new JSONObject();
            response.put("message", "Transfer successful");
            response.put("debit_transaction", pojoConverter.pojoToJson(debitTxn));
            response.put("credit_transaction", pojoConverter.pojoToJson(creditTxn));

            res.setContentType("application/json");
            res.getWriter().write(response.toString());

        } catch (IOException e) {
            e.printStackTrace();
            ErrorResponseUtil.send(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                new ErrorResponse("TaskException", 500, e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            ErrorResponseUtil.send(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                new ErrorResponse("Internal Error", 500, e.getMessage()));
        }
    }

    public void getTransaction(HttpServletRequest req, HttpServletResponse res) throws TaskException {
        RequestJsonConverter jsonConverter = new RequestJsonConverter();
        PojoJsonConverter pojoConverter = new PojoJsonConverter();
        TransactionDAO transactionDAO = new TransactionDAO();
        AccountDAO accountDAO = new AccountDAO();

        try {
            JSONObject json = jsonConverter.convertToJson(req);

            Long transactionId = json.has("transaction_id") ? json.getLong("transaction_id") : null;
            Long referenceNumber = json.has("reference_number") ? json.getLong("reference_number") : null;
            Long accountNumber = json.has("account_number") ? json.getLong("account_number") : null;
            Long from = json.has("from_date") ? json.getLong("from_date") : null;
            Long to = json.has("to_date") ? json.getLong("to_date") : null;

            SessionData sessionData = (SessionData) req.getAttribute("sessionData");
            long userId = sessionData.getUserId();

            // Case 1: Get by transaction ID or reference number (single record)
            if (transactionId != null || referenceNumber != null) {
                List<Transaction> transactions = transactionDAO.getFilteredTransactions(
                        transactionId, referenceNumber, null, null, null, null, null, 1, 0);

                if (transactions.isEmpty()) {
                    res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    res.getWriter().write("{\"message\":\"Transaction not found\"}");
                    return;
                }

                Transaction txn = transactions.get(0);
                // Only allow access if transaction belongs to the user
                Account acc = accountDAO.getAccountByNumber(txn.getAccountNumber());

                if (acc == null || acc.getUserId() != userId) {
                    ErrorResponseUtil.send(res, HttpServletResponse.SC_UNAUTHORIZED,
                            new ErrorResponse("Unauthorized", 403, "You do not have access to this transaction"));
                    return;
                }

                JSONObject jsonResp = pojoConverter.pojoToJson(txn);
                res.setContentType("application/json");
                res.getWriter().write(jsonResp.toString());
                return;
            }

            // Case 2: Get history for account number with date range
            if (accountNumber == null || from == null || to == null) {
                ErrorResponseUtil.send(res, HttpServletResponse.SC_BAD_REQUEST,
                        new ErrorResponse("Bad Request", 400, "account_number, from_date and to_date are required"));
                return;
            }

            // Check if the account belongs to the user
            Account acc = accountDAO.getAccountByNumber(accountNumber);
            if (acc == null || acc.getUserId() != userId) {
                ErrorResponseUtil.send(res, HttpServletResponse.SC_UNAUTHORIZED,
                        new ErrorResponse("Unauthorized", 403, "You do not own this account"));
                return;
            }

            List<Transaction> transactions = transactionDAO.getFilteredTransactions(
                    null, null, accountNumber, from, to, null, null, 100, 0);

            JSONArray jsonArr = new JSONArray();
            for (Transaction txn : transactions) {
                jsonArr.put(pojoConverter.pojoToJson(txn));
            }

            JSONObject result = new JSONObject();
            result.put("transactions", jsonArr);

            res.setContentType("application/json");
            res.getWriter().write(result.toString());

        } catch (IOException e) {
            e.printStackTrace();
            ErrorResponseUtil.send(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    new ErrorResponse("TaskException", 500, e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            ErrorResponseUtil.send(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    new ErrorResponse("Internal Error", 500, e.getMessage()));
        }
    }

    


    public void addBeneficiary(HttpServletRequest req, HttpServletResponse res) throws TaskException {
        RequestJsonConverter jsonConverter = new RequestJsonConverter();
        CustomerDAO customerDAO = new CustomerDAO();

        try {
            JSONObject json = jsonConverter.convertToJson(req);

            // Session info for customer
            SessionData sessionData = (SessionData) req.getAttribute("sessionData");
            long customerId = sessionData.getUserId();
            long doneBy = sessionData.getUserId(); // Customer is adding the beneficiary

            // Required fields from body
            if (!json.has("beneficiary_account_number") || !json.has("beneficiary_name")) {
                ErrorResponseUtil.send(res, HttpServletResponse.SC_BAD_REQUEST,
                    new ErrorResponse("Bad Request", 400, "Missing required fields: beneficiary_account_number, beneficiary_name"));
                return;
            }

            long beneficiaryAccountNumber = json.getLong("beneficiary_account_number");
            String beneficiaryName = json.getString("beneficiary_name");
            String beneficiaryIFSC = json.getString("beneficiary_ifsc_code");

            Beneficiary beneficiary = new Beneficiary();
            beneficiary.setCustomerId(customerId);
            beneficiary.setBeneficiaryAccountNumber(beneficiaryAccountNumber);
            beneficiary.setBeneficiaryName(beneficiaryName);
            beneficiary.setBeneficiaryIfscCode(beneficiaryIFSC);
            beneficiary.setCreatedAt(System.currentTimeMillis());
            beneficiary.setModifiedAt(System.currentTimeMillis());
            beneficiary.setModifiedBy(doneBy);

            long savedId = customerDAO.addBeneficiary(beneficiary);

            JSONObject jsonResp = new JSONObject();
            jsonResp.put("status", "Beneficiary added successfully");
            jsonResp.put("beneficiary_id", savedId);

            res.setContentType("application/json");
            res.getWriter().write(jsonResp.toString());

        } catch (IOException e) {
            e.printStackTrace();
            ErrorResponseUtil.send(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                new ErrorResponse("TaskException", 500, e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            ErrorResponseUtil.send(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                new ErrorResponse("Internal Error", 500, e.getMessage()));
        }
    }

    


    public void getBeneficiary(HttpServletRequest req, HttpServletResponse res) throws TaskException {
        CustomerDAO customerDAO = new CustomerDAO();
        PojoJsonConverter converter = new PojoJsonConverter();

        try {
            // Get customer session info
            SessionData sessionData = (SessionData) req.getAttribute("sessionData");
            long customerId = sessionData.getUserId();

            // Fetch beneficiaries from DB
            List<Beneficiary> beneficiaries = customerDAO.getBeneficiariesByCustomerId(customerId);

            JSONArray jsonArray = new JSONArray();
            for (Beneficiary b : beneficiaries) {
                JSONObject jsonObj = converter.pojoToJson(b);
                jsonArray.put(jsonObj);
            }

            JSONObject responseJson = new JSONObject();
            responseJson.put("beneficiaries", jsonArray);
            responseJson.put("message", "Beneficiaries fetched successfully");

            res.setContentType("application/json");
            res.getWriter().write(responseJson.toString());

        } catch (IOException e) {
            e.printStackTrace();
            ErrorResponseUtil.send(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                new ErrorResponse("TaskException", 500, e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            ErrorResponseUtil.send(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                new ErrorResponse("Error", 500, e.getMessage()));
        }
    }

}



// For customer testing
// Customer1 : 100000028
// Pwd:  !ryy5X+F




























