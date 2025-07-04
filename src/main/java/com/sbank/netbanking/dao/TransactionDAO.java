package com.sbank.netbanking.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.sbank.netbanking.dbconfig.ConnectionManager;
import com.sbank.netbanking.exceptions.ExceptionMessages;
import com.sbank.netbanking.exceptions.TaskException;
import com.sbank.netbanking.model.Transaction;
import com.sbank.netbanking.model.Transaction.TransactionStatus;
import com.sbank.netbanking.model.Transaction.TransactionType;

public class TransactionDAO {

	public Transaction deposit(Long toAccountNumber, double amount, long doneBy, 
			TransactionType transactionType, long transactionId, Long fromAccountNumber, String ifcsCode)
					throws TaskException {
		
	    String getAccountSQL = "SELECT balance, user_id, status FROM accounts WHERE account_number = ?";
	    String updateAccountSQL = "UPDATE accounts SET balance = ?, modified_at = ?, modified_by = ? WHERE account_number = ?";
	    String insertTransactionSQL = "INSERT INTO transactions (transaction_id, user_id, account_number,"
	    		+ " amount, type, status, timestamp, done_by, closing_balance,"
	    		+ " beneficiery_account_number,"
	    		+ " ifsc_code) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
	        	
	        	
	            System.out.println(toAccountNumber);
		        System.out.println(amount);
		        
	            // 1. Fetch account details
	            getAccStmt.setLong(1, toAccountNumber);
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
	            updateAccStmt.setLong(4, toAccountNumber);
	            updateAccStmt.executeUpdate();


	            // 4. Insert transaction
	            insertTxnStmt.setLong(1, transactionId);
	            insertTxnStmt.setLong(2, userId);
	            insertTxnStmt.setLong(3, toAccountNumber);
	            insertTxnStmt.setDouble(4, amount);
	            insertTxnStmt.setString(5, type);
	            insertTxnStmt.setString(6, status);
	            insertTxnStmt.setLong(7, currentTime);
	            insertTxnStmt.setLong(8, doneBy);
	            insertTxnStmt.setDouble(9, updatedBalance);
	            
	           
	            insertTxnStmt.setObject(10, fromAccountNumber); // handles null
	            insertTxnStmt.setObject(11, ifcsCode);          // handles null


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
	            txn.setAccountNumber(toAccountNumber);
	            txn.setUserId(userId);
	            txn.setAmount(amount);
	            txn.setType(TransactionType.valueOf(type));
	            txn.setStatus(TransactionStatus.valueOf(status));
	            txn.setTimestamp(currentTime);
	            txn.setDoneBy(doneBy);
	            txn.setClosingBalance(updatedBalance);

	            if(fromAccountNumber !=null) {
	            txn.setBeneficiaryAccountNumber(fromAccountNumber);
	            }
	           
	            if(ifcsCode!=null) {
		            txn.setIfscCode(ifcsCode);
		            }
		            
	            return txn;

	        }
	    } catch (SQLException e) {
	        throw new TaskException("Failed to perform deposit", e);
	    } catch (Exception e) {
	    	e.printStackTrace();
	        throw new TaskException(ExceptionMessages.TRANSACTION_FAILED, e);
	    }
	}

	
	
	
	
	public Transaction withdraw(Long fromAccountNumber, double amount, long doneBy,
			TransactionType transactionType, long transactionId, Long toAccountNumber, String ifcsCode) 
					throws TaskException {
		
	    String getAccountSQL = "SELECT balance, user_id, status FROM accounts WHERE account_number = ?";
	    String updateAccountSQL = "UPDATE accounts SET balance = ?, modified_at = ?, modified_by = ? "
	    		+ "WHERE account_number = ?";
	    
	    String insertTransactionSQL = "INSERT INTO transactions (transaction_id, user_id,"
	    		+ " account_number, amount, type, status, timestamp, done_by, closing_balance, "
	    		+ " beneficiery_account_number, ifsc_code) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
	            getAccStmt.setLong(1, fromAccountNumber);
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
	                updateAccStmt.setLong(4, fromAccountNumber);
	                updateAccStmt.executeUpdate();
	            }
	            
	            

	            // 4. Insert transaction record
	    

	            insertTxnStmt.setLong(1, transactionId); // transaction_id placeholder
	            insertTxnStmt.setLong(2, userId);
	            insertTxnStmt.setLong(3, fromAccountNumber);
	            insertTxnStmt.setDouble(4, amount);
	            insertTxnStmt.setString(5, transactionType.name());
	            insertTxnStmt.setString(6, status);
	            insertTxnStmt.setLong(7, currentTime);
	            insertTxnStmt.setLong(8, doneBy);
	            insertTxnStmt.setDouble(9, updatedBalance);
	            insertTxnStmt.setLong(10, fromAccountNumber);
	            insertTxnStmt.setString(11, ifcsCode);
	            insertTxnStmt.executeUpdate();

	            // 5. Return response POJO
	            Transaction txn = new Transaction();
	            txn.setAccountNumber(fromAccountNumber);
	            txn.setUserId(userId);
	            txn.setTransactionReferenceNumber(referenceNumber);
	            txn.setAmount(amount);
	            txn.setType(transactionType);
	            txn.setStatus(TransactionStatus.valueOf(status));
	            txn.setTimestamp(currentTime);
	            txn.setDoneBy(doneBy);
	            txn.setClosingBalance(updatedBalance);
	            
	            if(fromAccountNumber!=null) {
	            txn.setBeneficiaryAccountNumber(fromAccountNumber);
	            }
	           
	            if(ifcsCode!=null) {
		            txn.setIfscCode(ifcsCode);
		            }
		            
	            return txn;

	        }
	    } catch (SQLException e) {
	        throw new TaskException("Failed to perform withdrawal", e);
	    } catch (Exception e) {
	        throw new TaskException(ExceptionMessages.WRONG_ACTION, e);
	    }
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public List<Transaction> getFilteredTransactions(
	        Long txnId, Long refNum, Long accNum, Long customerId,
	        Long from, Long to, String type, String status,
	        int limit, int offset) throws TaskException {

	    List<Transaction> transactions = new ArrayList<>();

	    try (ConnectionManager cm = new ConnectionManager()) {
	        cm.initConnection();
	        Connection conn = cm.getConnection();

	        if (txnId != null) {
	            transactions.addAll(fetchBySingleParam(conn, "transaction_id", txnId));
	        } else if (refNum != null) {
	            transactions.addAll(fetchBySingleParam(conn, "transaction_reference_number", refNum));
	        } else if (accNum != null) {
	            transactions.addAll(fetchByAccount(conn, accNum, from, to, type, status, limit, offset));
	        } else if (customerId != null && from != null && to != null) {
	            // Enforce hard limit of 10 per account
	            int perAccountLimit = Math.min(limit, 10);

	            String accountSQL = "SELECT account_number FROM accounts WHERE user_id = ?";
	            try (PreparedStatement ps = conn.prepareStatement(accountSQL)) {
	                ps.setLong(1, customerId);
	                ResultSet rs = ps.executeQuery();

	                while (rs.next()) {
	                    Long acct = rs.getLong("account_number");

	                    // Fetch last perAccountLimit transactions per account
	                    List<Transaction> accTxns = fetchByAccount(
	                        conn, acct, from, to, type, status, perAccountLimit, 0
	                    );
	                    transactions.addAll(accTxns);
	                }
	            }
	        }

	    } catch (SQLException e) {
	        throw new TaskException("SQL Error while fetching transactions", e);
	    } catch (Exception e) {
	        throw new TaskException("Failed to fetch transactions", e);
	    }

	    return transactions;
	}


	
	public List<Transaction> getTransactionsByUserAndDateRange(long userId, long fromTimestamp, long toTimestamp)
	        throws TaskException {

	    List<Transaction> transactions = new ArrayList<>();
	    String sql = "SELECT * FROM transactions WHERE done_by = ? AND timestamp BETWEEN ? AND ? ORDER BY timestamp DESC";

	    try (ConnectionManager cm = new ConnectionManager()) {
	        cm.initConnection();
	        Connection conn = cm.getConnection();

	        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
	            stmt.setLong(1, userId);
	            stmt.setLong(2, fromTimestamp);
	            stmt.setLong(3, toTimestamp);

	            ResultSet rs = stmt.executeQuery();

	            while (rs.next()) {
	                Transaction txn = new Transaction();
	                txn.setTransactionReferenceNumber(rs.getLong("transaction_reference_number"));
	                txn.setTransactionId(rs.getLong("transaction_id"));
	                txn.setAccountNumber(rs.getLong("account_number"));
	                txn.setAmount(rs.getDouble("amount"));
	                txn.setType(Transaction.TransactionType.valueOf(rs.getString("type")));
	                txn.setStatus(Transaction.TransactionStatus.valueOf(rs.getString("status")));
	                txn.setTimestamp(rs.getLong("timestamp"));
	                txn.setDoneBy(rs.getLong("done_by"));
	                txn.setClosingBalance(rs.getDouble("closing_balance"));
	                txn.setUserId(rs.getLong("user_id"));

	                try {
	                    txn.setBeneficiaryAccountNumber(rs.getLong("beneficiery_account_number"));
	                } catch (Exception ignore) {}

	                try {
	                    txn.setIfscCode(rs.getString("ifsc_code"));
	                } catch (Exception ignore) {}

	                transactions.add(txn);
	            }

	        }
	    } catch (SQLException e) {
	        throw new TaskException("Failed to fetch transactions by user and date range", e);
	    } catch (Exception e) {
	        throw new TaskException(ExceptionMessages.DATABASE_CONNECTION_FAILED, e);
	    }

	    return transactions;
	}

	private List<Transaction> fetchByAccount(Connection conn, Long accNum, Long from, Long to,
            String type, String status, int limit, int offset)
			throws SQLException {
			
			List<Object> params = new ArrayList<>();
			StringBuilder sql = new StringBuilder("SELECT * FROM transactions WHERE account_number = ?");
			
			params.add(accNum);
			
			if (from != null && to != null) {
			sql.append(" AND timestamp BETWEEN ? AND ?");
			params.add(from);
			params.add(to);
			}
			
			if (type != null) {
			sql.append(" AND type = ?");
			params.add(type);
			}
			
			if (status != null) {
			sql.append(" AND status = ?");
			params.add(status);
			}
			
			sql.append(" ORDER BY timestamp DESC LIMIT ? OFFSET ?");
			params.add(limit);
			params.add(offset);
			
			List<Transaction> result = new ArrayList<>();
			try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
			for (int i = 0; i < params.size(); i++) {
			stmt.setObject(i + 1, params.get(i));
			}
			
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
			result.add(mapTransaction(rs));
			}
			}
			
			return result;
}
	
	
	
	
	
	
	private List<Transaction> fetchBySingleParam(Connection conn, String field, Long value) throws SQLException {
	    List<Transaction> result = new ArrayList<>();
	    String sql = "SELECT * FROM transactions WHERE " + field + " = ?";

	    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
	        stmt.setLong(1, value);
	        ResultSet rs = stmt.executeQuery();

	        while (rs.next()) {
	            result.add(mapTransaction(rs));
	        }
	    }

	    return result;
	}

	
	private Transaction mapTransaction(ResultSet rs) throws SQLException {
	    Transaction t = new Transaction();
	    t.setTransactionReferenceNumber(rs.getLong("transaction_reference_number"));
	    t.setTransactionId(rs.getLong("transaction_id"));
	    t.setAccountNumber(rs.getLong("account_number"));
	    t.setAmount(rs.getDouble("amount"));
	    t.setType(Transaction.TransactionType.valueOf(rs.getString("type")));
	    t.setStatus(Transaction.TransactionStatus.valueOf(rs.getString("status")));
	    t.setTimestamp(rs.getLong("timestamp"));
	    t.setDoneBy(rs.getLong("done_by"));
	    t.setClosingBalance(rs.getDouble("closing_balance"));
	    t.setUserId(rs.getLong("user_id"));

	    try {
	        t.setBeneficiaryAccountNumber(rs.getLong("beneficiery_account_number"));
	    } catch (Exception ignore) {}

	    try {
	        t.setIfscCode(rs.getString("ifsc_code"));
	    } catch (Exception ignore) {}

	    return t;
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	




}
