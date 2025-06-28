package com.sbank.netbanking.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.sbank.netbanking.dbconfig.ConnectionManager;
import com.sbank.netbanking.exceptions.ExceptionMessages;
import com.sbank.netbanking.exceptions.TaskException;
import com.sbank.netbanking.model.Transaction;
import com.sbank.netbanking.model.Transaction.TransactionStatus;
import com.sbank.netbanking.model.Transaction.TransactionType;

public class TransactionDAO {

	public Transaction deposit(Long accountNumber, double amount, long doneBy, TransactionType transactionType, long transactionId) throws TaskException {
	    String getAccountSQL = "SELECT balance, user_id, status FROM accounts WHERE account_number = ?";
	    String updateAccountSQL = "UPDATE accounts SET balance = ?, modified_at = ?, modified_by = ? WHERE account_number = ?";
	    String insertTransactionSQL = "INSERT INTO transactions (transaction_id, user_id, account_number, amount, type, status, timestamp, done_by, closing_balance) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

	    long currentTime = System.currentTimeMillis();
	    String status = TransactionStatus.SUCCESS.name();
	    String type = transactionType.name();

	    try (ConnectionManager connectionManager = new ConnectionManager()) {
	        connectionManager.initConnection();
	        Connection conn = connectionManager.getConnection();

	        try (
	            PreparedStatement getAccStmt = conn.prepareStatement(getAccountSQL);
	            PreparedStatement updateAccStmt = conn.prepareStatement(updateAccountSQL);
	            PreparedStatement insertTxnStmt = conn.prepareStatement(insertTransactionSQL, Statement.RETURN_GENERATED_KEYS)
	        ) {
	            // 1. Fetch account details
	            getAccStmt.setLong(1, accountNumber);
	            ResultSet rs = getAccStmt.executeQuery();

	            if (!rs.next()) {
	                throw new TaskException("Account not found");
	            }

	            double currentBalance = rs.getDouble("balance");
	            long userId = rs.getLong("user_id");
	            String accStatus = rs.getString("status");

	            if (!"ACTIVE".equalsIgnoreCase(accStatus)) {
	                throw new TaskException("Account is not active");
	            }

	            // 2. Update account balance
	            double updatedBalance = currentBalance + amount;
	            updateAccStmt.setDouble(1, updatedBalance);
	            updateAccStmt.setLong(2, currentTime);
	            updateAccStmt.setLong(3, doneBy);
	            updateAccStmt.setLong(4, accountNumber);
	            updateAccStmt.executeUpdate();


	            // 4. Insert transaction
	            insertTxnStmt.setLong(1, transactionId);
	            insertTxnStmt.setLong(2, userId);
	            insertTxnStmt.setLong(3, accountNumber);
	            insertTxnStmt.setDouble(4, amount);
	            insertTxnStmt.setString(5, type);
	            insertTxnStmt.setString(6, status);
	            insertTxnStmt.setLong(7, currentTime);
	            insertTxnStmt.setLong(8, doneBy);
	            insertTxnStmt.setDouble(9, updatedBalance);
	            insertTxnStmt.executeUpdate();

	            // 5. Get generated reference number
	            long referenceNumber;
	            try (ResultSet keys = insertTxnStmt.getGeneratedKeys()) {
	                if (keys.next()) {
	                    referenceNumber = keys.getLong(1);
	                } else {
	                    throw new TaskException("Failed to retrieve transaction reference number");
	                }
	            }

	            // 6. Return POJO
	            Transaction txn = new Transaction();
	            txn.setTransactionId(transactionId);
	            txn.setTransactionReferenceNumber(referenceNumber);
	            txn.setAccountNumber(accountNumber);
	            txn.setUserId(userId);
	            txn.setAmount(amount);
	            txn.setType(TransactionType.valueOf(type));
	            txn.setStatus(TransactionStatus.valueOf(status));
	            txn.setTimestamp(currentTime);
	            txn.setDoneBy(doneBy);
	            txn.setClosingBalance(updatedBalance);
	            return txn;

	        }
	    } catch (SQLException e) {
	        throw new TaskException("Failed to perform deposit", e);
	    } catch (Exception e) {
	        throw new TaskException(ExceptionMessages.WRONG_ACTION, e);
	    }
	}

	
	
	
	
	public Transaction withdraw(Long accountNumber, double amount, long doneBy, TransactionType transactionType, long transactionId) throws TaskException {
	    String getAccountSQL = "SELECT balance, user_id, status FROM accounts WHERE account_number = ?";
	    String updateAccountSQL = "UPDATE accounts SET balance = ?, modified_at = ?, modified_by = ? WHERE account_number = ?";
	    String insertTransactionSQL = "INSERT INTO transactions (transaction_id, user_id, account_number, amount, type, status, timestamp, done_by, closing_balance) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

	    long currentTime = System.currentTimeMillis();
	    long referenceNumber = currentTime;  // Unique per transaction row
	    String status;
	    double updatedBalance;

	    try (ConnectionManager connectionManager = new ConnectionManager()) {
	        connectionManager.initConnection();
	        Connection conn = connectionManager.getConnection();

	        try (
	            PreparedStatement getAccStmt = conn.prepareStatement(getAccountSQL);
	            PreparedStatement updateAccStmt = conn.prepareStatement(updateAccountSQL);
	            PreparedStatement insertTxnStmt = conn.prepareStatement(insertTransactionSQL)
	        ) {
	            // 1. Fetch account
	            getAccStmt.setLong(1, accountNumber);
	            ResultSet rs = getAccStmt.executeQuery();

	            if (!rs.next()) {
	                throw new TaskException("Account not found");
	            }

	            double currentBalance = rs.getDouble("balance");
	            long userId = rs.getLong("user_id");
	            String accStatus = rs.getString("status");

	            if (!"ACTIVE".equals(accStatus)) {
	                throw new TaskException("Account is not active");
	            }

	            // 2. Check balance
	            if (amount > currentBalance) {
	                status = "FAILED";
	                updatedBalance = currentBalance;
	            } else {
	                status = "SUCCESS";
	                updatedBalance = currentBalance - amount;

	                // 3. Update balance only if successful
	                updateAccStmt.setDouble(1, updatedBalance);
	                updateAccStmt.setLong(2, currentTime);
	                updateAccStmt.setLong(3, doneBy);
	                updateAccStmt.setLong(4, accountNumber);
	                updateAccStmt.executeUpdate();
	            }
	            
	            

	            // 4. Insert transaction record
	    

	            insertTxnStmt.setLong(1, transactionId); // transaction_id placeholder
	            insertTxnStmt.setLong(2, userId);
	            insertTxnStmt.setLong(3, accountNumber);
	            insertTxnStmt.setDouble(4, amount);
	            insertTxnStmt.setString(5, transactionType.name());
	            insertTxnStmt.setString(6, status);
	            insertTxnStmt.setLong(7, currentTime);
	            insertTxnStmt.setLong(8, doneBy);
	            insertTxnStmt.setDouble(9, updatedBalance);
	            insertTxnStmt.executeUpdate();

	            // 5. Return response POJO
	            Transaction txn = new Transaction();
	            txn.setAccountNumber(accountNumber);
	            txn.setUserId(userId);
	            txn.setTransactionReferenceNumber(referenceNumber);
	            txn.setAmount(amount);
	            txn.setType(transactionType);
	            txn.setStatus(TransactionStatus.valueOf(status));
	            txn.setTimestamp(currentTime);
	            txn.setDoneBy(doneBy);
	            txn.setClosingBalance(updatedBalance);
	            return txn;

	        }
	    } catch (SQLException e) {
	        throw new TaskException("Failed to perform withdrawal", e);
	    } catch (Exception e) {
	        throw new TaskException(ExceptionMessages.WRONG_ACTION, e);
	    }
	}


}
