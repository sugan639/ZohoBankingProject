package com.sbank.netbanking.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.sbank.netbanking.dbconfig.ConnectionManager;
import com.sbank.netbanking.exceptions.ExceptionMessages;
import com.sbank.netbanking.exceptions.TaskException;
import com.sbank.netbanking.model.Account;
import com.sbank.netbanking.model.Account.Status;
import com.sbank.netbanking.model.Employee;
import com.sbank.netbanking.model.NewCustomer;
import com.sbank.netbanking.model.Transaction;
import com.sbank.netbanking.model.Transaction.TransactionType;
import com.sbank.netbanking.model.User.Role;

public class AdminDAO {

	 public Employee getEmployeeById(long userId) throws TaskException {
	        String sql = "SELECT u.user_id, u.name, u.email, u.mobile_number, u.role, " +
	                     "u.created_at, u.modified_at, u.modified_by, " +
	                     "e.employee_id, e.branch_id " +
	                     "FROM users u INNER JOIN employees e ON u.user_id = e.employee_id " +
	                     "WHERE u.user_id = ?";

	        try (ConnectionManager connectionManager = new ConnectionManager()) {
	            connectionManager.initConnection();
	            Connection conn = connectionManager.getConnection();

	            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
	                stmt.setLong(1, userId);

	                try (ResultSet rs = stmt.executeQuery()) {
	                    if (rs.next()) {
	                        Employee employee = new Employee();
	                        employee.setEmployeeId(rs.getLong("employee_id"));
	                        employee.setName(rs.getString("name"));
	                        employee.setEmail(rs.getString("email"));
	                        employee.setMobileNumber(rs.getLong("mobile_number"));
	                        employee.setRole(Role.valueOf(rs.getString("role")));
	                        employee.setCreatedAt(rs.getLong("created_at"));
	                        employee.setModifiedAt(rs.getLong("modified_at"));
	                        employee.setModifiedBy(rs.getLong("modified_by"));
	                        employee.setBranchId(rs.getLong("branch_id"));
	                        return employee;
	                    }
	                }

	            } catch (SQLException e) {
	                throw new TaskException("Failed to fetch employee by ID", e);
	            }

	        } catch (Exception e) {
	            throw new TaskException(ExceptionMessages.DATABASE_CONNECTION_FAILED, e);
	        }

	        return null; // not found
	    }

    
	// Adding new employee
	public Employee addNewEmployee(String name, String password, String email, long mobileNumber, long branchId, Role role, long createdBy) throws TaskException {
	    String insertUserSQL = "INSERT INTO users (name, password, email, mobile_number, role, created_at, modified_at, modified_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
	    String insertEmployeeSQL = "INSERT INTO employees (employee_id, branch_id) VALUES (?, ?)";
	    long currentTime = System.currentTimeMillis();

	    try (ConnectionManager connectionManager = new ConnectionManager()) {
	        connectionManager.initConnection();
	        Connection conn = connectionManager.getConnection();

	        try {
	            conn.setAutoCommit(false);  // Setting before using commit

	            try (
	                PreparedStatement userStmt = conn.prepareStatement(insertUserSQL, Statement.RETURN_GENERATED_KEYS);
	                PreparedStatement empStmt = conn.prepareStatement(insertEmployeeSQL)
	            ) {
	                userStmt.setString(1, name);
	                userStmt.setString(2, password);
	                userStmt.setString(3, email);
	                userStmt.setLong(4, mobileNumber);
	                userStmt.setString(5, role.name());  // Converts enum to "EMPLOYEE", "ADMIN", etc.
	                userStmt.setLong(6, currentTime);
	                userStmt.setLong(7, currentTime);
	                userStmt.setLong(8, createdBy); // Who created this account

	                int userRows = userStmt.executeUpdate();
	                if (userRows <= 0) {
	                    conn.rollback();
	                    throw new TaskException("Failed to insert into users table");
	                }

	                ResultSet generatedKeys = userStmt.getGeneratedKeys();
	                if (!generatedKeys.next()) {
	                    conn.rollback();
	                    throw new TaskException("Failed to retrieve generated user_id");
	                }

	                long userId = generatedKeys.getLong(1);

	                empStmt.setLong(1, userId);
	                empStmt.setLong(2, branchId);
	                int empRows = empStmt.executeUpdate();
	                if (empRows <= 0) {
	                    conn.rollback();
	                    throw new TaskException("Failed to insert into employees table");
	                }

	                conn.commit();  // Everything succeeded

	                // Build and return employee DTO
	                Employee emp = new Employee();
	                emp.setEmployeeId(userId);
	                emp.setName(name);
	                emp.setEmail(email);
	                emp.setMobileNumber(mobileNumber);
	                emp.setBranchId(branchId);
	                emp.setRole(role); // "ADMIN" or "EMPLOYEE"

	                return emp;

	            } catch (SQLException e) {
	                conn.rollback();  // Will work since autocommit is disabled
	                throw new TaskException("Failed to insert employee data", e);
	            } finally {
	                conn.setAutoCommit(true);  
	            }

	        } catch (SQLException e) {
	            throw new TaskException("Transaction error", e);
	        }

	    } catch (Exception e) {
	        throw new TaskException( e);
	    }
	}

    
	
	public NewCustomer addNewCustomer(String name, String password, String email, long mobileNumber,
            long dobMillis, String address, long aadharNumber,
            String panNumber, long createdBy, String role) throws TaskException {
			
			String insertUserSQL = "INSERT INTO users (name, password, email, mobile_number, role, created_at, modified_at, modified_by) VALUES (?, ?, ?, ?, 'CUSTOMER', ?, ?, ?)";
			String insertCustomerSQL = "INSERT INTO customers (customer_id, dob, address, aadhar_number, pan_number) VALUES (?, ?, ?, ?, ?)";
			long currentTime = System.currentTimeMillis();
			
			try (ConnectionManager connectionManager = new ConnectionManager()) {
			connectionManager.initConnection();
			Connection conn = connectionManager.getConnection();
			
			try (
			PreparedStatement userStmt = conn.prepareStatement(insertUserSQL, Statement.RETURN_GENERATED_KEYS);
			PreparedStatement custStmt = conn.prepareStatement(insertCustomerSQL)
			) {
			// Insert into users table
			userStmt.setString(1, name);
			userStmt.setString(2, password);
			userStmt.setString(3, email);
			userStmt.setLong(4, mobileNumber);
			userStmt.setLong(5, currentTime);
			userStmt.setLong(6, currentTime);
			userStmt.setLong(7, createdBy);
			
			int row = userStmt.executeUpdate();
			if (row == 0) {
			throw new TaskException("Failed to insert into users table");
			}
			
			ResultSet generatedKeys = userStmt.getGeneratedKeys();
			if (!generatedKeys.next()) {
			throw new TaskException("Failed to retrieve generated user_id");
			}
			long userId = generatedKeys.getLong(1);
			
			// Insert into customers table
			custStmt.setLong(1, userId);
			custStmt.setLong(2, dobMillis);
			custStmt.setString(3, address);
			custStmt.setLong(4, aadharNumber);
			custStmt.setString(5, panNumber);
			
			int row2 = custStmt.executeUpdate();
			if (row2 == 0) {
			throw new TaskException("Failed to insert into customers table");
			}
			
			// Prepare return object
			NewCustomer customer = new NewCustomer();
			customer.setCustomerId(userId);
			customer.setName(name);
			

			customer.setEmail(email);
			customer.setMobileNumber(mobileNumber);
			customer.setDob(dobMillis);
			customer.setAddress(address);
			customer.setAadharNumber(aadharNumber);
			customer.setPanNumber(panNumber);
			customer.setRole("CUSTOMER");
			customer.setCreatedAt(currentTime);
			customer.setModifiedAt(currentTime);
			customer.setModifiedBy(createdBy);
			
			
			return customer;
			
			} catch (SQLException e) {
			throw new TaskException("Failed to insert customer data", e);
		}
			} catch (Exception e) {
			throw new TaskException("Database error while adding customer", e);
	}
	}
	
	public void updateEmployeeFields(Long employeeId, Long branchId) throws TaskException {
	    if (branchId == null) return;

	    String sql = "UPDATE employees SET branch_id = ? WHERE employee_id = ?";
	    try (ConnectionManager connectionManager = new ConnectionManager()) {
            connectionManager.initConnection();
            Connection conn = connectionManager.getConnection();
	        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
	            stmt.setLong(1, branchId);
	            stmt.setLong(2, employeeId);
	            stmt.executeUpdate();
	        }
	    } catch (SQLException e) {
	        throw new TaskException("Failed to update employee data", e);
	    }
	    catch (Exception e) {
            throw new TaskException(ExceptionMessages.DATABASE_CONNECTION_FAILED, e);
        }

	}
	
	// AdminDAO.java
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
	                acc.setStatus(Status.ACTIVE);
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

	
	
	
	
	
	
	
	
	public Transaction performDeposit(Long accountNumber, double amount, long doneBy) throws TaskException {
	    String getAccountSQL = "SELECT balance, user_id, status FROM accounts WHERE account_number = ?";
	    String updateAccountSQL = "UPDATE accounts SET balance = ?, modified_at = ?, modified_by = ? WHERE account_number = ?";
	    String insertTransactionSQL = "INSERT INTO transactions (transaction_id, user_id, account_number, transaction_reference_number, amount, type, status, timestamp, done_by, closing_balance) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

	    long currentTime = System.currentTimeMillis();
	    long referenceNumber = currentTime;  // You may use UUIDs or a generator
	    String status = "SUCCESS";
	    String type = "DEPOSIT";

	    try (ConnectionManager connectionManager = new ConnectionManager()) {
	        connectionManager.initConnection();
	        Connection conn = connectionManager.getConnection();

	        try (
	            PreparedStatement getAccStmt = conn.prepareStatement(getAccountSQL);
	            PreparedStatement updateAccStmt = conn.prepareStatement(updateAccountSQL);
	            PreparedStatement insertTxnStmt = conn.prepareStatement(insertTransactionSQL)
	        ) {
	            // 1. Get existing account
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

	            // 2. Update balance
	            double updatedBalance = currentBalance + amount;
	            updateAccStmt.setDouble(1, updatedBalance);
	            updateAccStmt.setLong(2, currentTime);
	            updateAccStmt.setLong(3, doneBy);
	            updateAccStmt.setLong(4, accountNumber);
	            updateAccStmt.executeUpdate();

	            // 3. Insert transaction
	            insertTxnStmt.setLong(1, 0L); // transaction_id placeholder
	            insertTxnStmt.setLong(2, userId);
	            insertTxnStmt.setLong(3, accountNumber);
	            insertTxnStmt.setLong(4, referenceNumber);
	            insertTxnStmt.setDouble(5, amount);
	            insertTxnStmt.setString(6, type);
	            insertTxnStmt.setString(7, status);
	            insertTxnStmt.setLong(8, currentTime);
	            insertTxnStmt.setLong(9, doneBy);
	            insertTxnStmt.setDouble(10, updatedBalance);
	            insertTxnStmt.executeUpdate();

	            // Build and return Transaction POJO
	            Transaction txn = new Transaction();
	            txn.setAccountNumber(accountNumber);
	            txn.setUserId(userId);
	            txn.setTransactionReferenceNumber(referenceNumber);
	            txn.setAmount(amount);
	            txn.setType(Transaction.TransactionType.valueOf(type));
	            txn.setStatus(Transaction.Status.valueOf(status));
	            txn.setTimestamp(currentTime);
	            txn.setDoneBy(doneBy);
	            txn.setClosingBalance(updatedBalance);
	            return txn;

	        }
	    } catch (SQLException e) {
	        throw new TaskException("Failed to perform deposit", e);
	    } catch (Exception e) {
	        throw new TaskException(ExceptionMessages.DATABASE_CONNECTION_FAILED, e);
	    }
	}




	
   
    
}
