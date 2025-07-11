package com.sbank.netbanking.service;

import java.util.List;

import org.json.JSONObject;

import com.sbank.netbanking.dao.AccountDAO;
import com.sbank.netbanking.dao.TransactionDAO;
import com.sbank.netbanking.exceptions.TaskException;
import com.sbank.netbanking.model.Account;
import com.sbank.netbanking.model.Transaction;
import com.sbank.netbanking.model.User.Role;
import com.sbank.netbanking.util.PojoJsonConverter;

public class TransactionService {


    public JSONObject fetchTransactions(JSONObject json, Role requesterRole, Long requesterId) throws TaskException {
        final TransactionDAO transactionDAO = new TransactionDAO();
        final AccountDAO accountDAO = new AccountDAO();
        final PojoJsonConverter pojoConverter = new PojoJsonConverter();
        
    	Long customerId = json.optLong("customer_id", -1L);
        Long txnId = json.has("transaction_id") ? json.getLong("transaction_id") : null;
        Long refNum = json.has("transaction_reference_number") ? json.getLong("transaction_reference_number") : null;
        Long accNum = json.has("account_number") ? json.getLong("account_number") : null;
        Long from = json.has("from_date") ? json.getLong("from_date") : null;
        Long to = json.has("to_date") ? json.getLong("to_date") : null;
        String type = json.optString("type", null);
        String status = json.optString("status", null);
        int limit = Math.min(json.optInt("limit", 10), 10);
        int offset = json.optInt("offset", 0);
        

        boolean hasTxnId = txnId != null;
        boolean hasRefNum = refNum != null;
        boolean hasAccountFilter = accNum != null && from != null && to != null;
        boolean hasCustomerFilter = customerId > 0 && from != null && to != null;

        if (!(hasTxnId || hasRefNum || hasAccountFilter || hasCustomerFilter)) {
            throw new TaskException("Bad Request: Provide transaction_id OR reference_number OR (account_number or customer_id) with from_date and to_date");
        }

        
        JSONObject result = new JSONObject();

        // Handle customer filter with grouping by account
        if (hasCustomerFilter) {
            if (requesterRole == Role.CUSTOMER && !customerId.equals(requesterId)) {
                throw new TaskException("Unauthorized: Cannot access other customers’ transactions");
            }

            List<Account> accounts = accountDAO.getAccountsByUserId(customerId);
            JSONObject map = new JSONObject();

            for (Account acc : accounts) {
                List<Transaction> txns = transactionDAO.getFilteredTransactions(
                    null, null, acc.getAccountNumber(), null, from, to, type, status, limit, offset
                );
                
                map.put(String.valueOf(acc.getAccountNumber()), pojoConverter.pojoListToJsonArray(txns));
            }
            result.put("transactions", map);
        }
        else {
            // Account ownership check for customer
            if (requesterRole == Role.CUSTOMER && accNum != null) {
                Account acc = accountDAO.getAccountByNumber(accNum);
                if (acc == null || !acc.getUserId().equals(requesterId)) {
                    throw new TaskException("Forbidden: You do not own this account");
                }
            }

            List<Transaction> txns = transactionDAO.getFilteredTransactions(
                txnId, refNum, accNum, (requesterRole == Role.CUSTOMER ? requesterId : null),
                from, to, type, status, limit, offset
            );
            result.put("transactions", pojoConverter.pojoListToJsonArray(txns));
        }

        return result;
    }
}


