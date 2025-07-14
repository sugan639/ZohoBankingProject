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
import com.sbank.netbanking.model.Account;
import com.sbank.netbanking.model.Account.AccountStatus;

public class AccountDAO {
	
	public Account createCustomerAccount(Long userId, Long branchId, Double balance, Long createdBy) throws TaskException {
	    String sql = "INSERT INTO accounts (user_id, balance, branch_id, status, created_at, modified_at, modified_by) " +
	                 "VALUES (?, ?, ?, 'ACTIVE', ?, ?, ?)";

	    long currentTime = System.currentTimeMillis();

	    try (ConnectionManager connectionManager = new ConnectionManager()) {
	        connectionManager.initConnection();
	        Connection conn = connectionManager.getConnection();

	        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
	            stmt.setLong(1, userId);
	            stmt.setDouble(2, balance);
	            stmt.setLong(3, branchId);
	            stmt.setLong(4, currentTime);
	            stmt.setLong(5, currentTime);
	            stmt.setLong(6, createdBy);

	            int rows = stmt.executeUpdate();
	            if (rows == 0) {
	                throw new TaskException("Account creation failed.");
	            }

	            ResultSet keys = stmt.getGeneratedKeys();
	            if (keys.next()) {
	                long accountNumber = keys.getLong(1);
	                Account acc = new Account();
	                acc.setAccountNumber(accountNumber);
	                acc.setUserId(userId);
	                acc.setBranchId(branchId);
	                acc.setBalance(balance);
	                acc.setStatus(AccountStatus.ACTIVE);
	                acc.setCreatedAt(currentTime);
	                acc.setModifiedAt(currentTime);
	                acc.setModifiedBy(createdBy);
	                return acc;
	            } else {
	                throw new TaskException("Failed to retrieve account number.");
	            }
	        }

	    } catch (SQLException e) {
	        throw new TaskException("Failed to create account", e);
	    }
	    
	    
	    catch (Exception e) {
	        throw new TaskException(ExceptionMessages.DATABASE_CONNECTION_FAILED, e);
	    }
	  
	    
	    
	}
	

    public Account getAccountByNumber(long accountNumber) throws TaskException {
        String sql = "SELECT * FROM accounts WHERE account_number = ?";
        try (ConnectionManager cm = new ConnectionManager()) {
            cm.initConnection();
            Connection conn = cm.getConnection();

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, accountNumber);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Account account = new Account();
                        account.setAccountNumber(rs.getLong("account_number"));
                        account.setUserId(rs.getLong("user_id"));
                        account.setBalance(rs.getDouble("balance"));
                        account.setBranchId(rs.getLong("branch_id"));
                        account.setStatus(AccountStatus.valueOf(rs.getString("status")));
                        account.setCreatedAt(rs.getLong("created_at"));
                        account.setModifiedAt(rs.getLong("modified_at"));
                        account.setModifiedBy(rs.getLong("modified_by"));
                        return account;
                    }
                }
            }
        } catch (SQLException e) {
            throw new TaskException("Failed to fetch account", e);
        } catch (Exception e) {
            throw new TaskException("Database connection error", e);
        }
        return null;
    }

    public void updateBalance(long accountNumber, double newBalance, long modifiedBy) throws TaskException {
        String sql = "UPDATE accounts SET balance = ?, modified_at = ?, modified_by = ? WHERE account_number = ?";
        long modifiedAt = System.currentTimeMillis();
        try (ConnectionManager cm = new ConnectionManager()) {
            cm.initConnection();
            Connection conn = cm.getConnection();

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setDouble(1, newBalance);
                stmt.setLong(2, modifiedAt);
                stmt.setLong(3, modifiedBy);
                stmt.setLong(4, accountNumber);
                stmt.executeUpdate();
            }

        } catch (SQLException e) {
            throw new TaskException("Failed to update account balance", e);
        } catch (Exception e) {
            throw new TaskException("Database connection error", e);
        }
    }
    

    public List<Account> getAccountsByUserId(long userId) throws TaskException {
        String sql = "SELECT * FROM accounts WHERE user_id = ?";
        List<Account> accountList = new ArrayList<>();

        try (ConnectionManager connectionManager = new ConnectionManager()) {
            connectionManager.initConnection();
            Connection conn = connectionManager.getConnection();

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, userId);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    Account acc = new Account();
                    acc.setAccountNumber(rs.getLong("account_number"));
                    acc.setUserId(rs.getLong("user_id"));
                    acc.setBalance(rs.getDouble("balance"));
                    acc.setBranchId(rs.getLong("branch_id"));
                    acc.setStatus(AccountStatus.valueOf(rs.getString("status")));
                    acc.setCreatedAt(rs.getLong("created_at"));
                    acc.setModifiedAt(rs.getLong("modified_at"));
                    acc.setModifiedBy(rs.getLong("modified_by"));
                    accountList.add(acc);
                }
            }
        } catch (SQLException e) {
            throw new TaskException("Failed to fetch accounts", e);
        } catch (Exception e) {
            throw new TaskException(ExceptionMessages.DATABASE_CONNECTION_FAILED, e);
        }

        return accountList;
    }

    

	public void changeAccountStatus(Long accountNumber, String status, long modifiedBy) throws TaskException {
	    String sql = "UPDATE accounts SET status = ?, modified_at = ?, modified_by = ? WHERE account_number = ?";
	    long currentTime = System.currentTimeMillis();

	    try (ConnectionManager connectionManager = new ConnectionManager()) {
	        connectionManager.initConnection();
	        Connection conn = connectionManager.getConnection();

	        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
	            stmt.setString(1, status);
	            stmt.setLong(2, currentTime);
	            stmt.setLong(3, modifiedBy);
	            stmt.setLong(4, accountNumber);
	            stmt.executeUpdate();
	        }
	    } catch (SQLException e) {
	        throw new TaskException("Failed to update account status", e);
	    } catch (Exception e) {
	        throw new TaskException(ExceptionMessages.DATABASE_CONNECTION_FAILED, e);
	    }
	}

	public void deleteAccount(Long accountNumber) throws TaskException {
	    String sql = "DELETE FROM accounts WHERE account_number = ?";

	    try (ConnectionManager connectionManager = new ConnectionManager()) {
	        connectionManager.initConnection();
	        Connection conn = connectionManager.getConnection();

	        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
	            stmt.setLong(1, accountNumber);
	            stmt.executeUpdate();
	        }
	    } catch (SQLException e) {
	        throw new TaskException("Failed to delete account", e);
	    } catch (Exception e) {
	        throw new TaskException(ExceptionMessages.DATABASE_CONNECTION_FAILED, e);
	    }
	}
	
	public List<Account> getAccountsByCustomerAndBranch(Long customerId, Long branchId) throws TaskException {
	    List<Account> accountList = new ArrayList<>();
	    String sql = "SELECT * FROM accounts WHERE user_id = ? AND branch_id = ?";

	    try (ConnectionManager cm = new ConnectionManager()) {
	        cm.initConnection();
	        Connection conn = cm.getConnection();

	        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
	            stmt.setLong(1, customerId);
	            stmt.setLong(2, branchId);

	            ResultSet rs = stmt.executeQuery();
	            while (rs.next()) {
	            	  Account acc = new Account();
	                    acc.setAccountNumber(rs.getLong("account_number"));
	                    acc.setUserId(rs.getLong("user_id"));
	                    acc.setBalance(rs.getDouble("balance"));
	                    acc.setBranchId(rs.getLong("branch_id"));
	                    acc.setStatus(AccountStatus.valueOf(rs.getString("status")));
	                    acc.setCreatedAt(rs.getLong("created_at"));
	                    acc.setModifiedAt(rs.getLong("modified_at"));
	                    acc.setModifiedBy(rs.getLong("modified_by"));
	                    accountList.add(acc);
	            }
	        }
	    } catch (SQLException e) {
	        throw new TaskException("Failed to fetch accounts for given customerId and branchId", e);
	    } catch (Exception e) {
	        throw new TaskException(ExceptionMessages.DATABASE_CONNECTION_FAILED, e);
	    }

	    return accountList;
	}

	
	
	
	
	
	
	
	
	
	
	
}

