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
import com.sbank.netbanking.model.Employee;
import com.sbank.netbanking.model.NewCustomer;
import com.sbank.netbanking.model.Account.AccountStatus;
import com.sbank.netbanking.model.User.Role;

public class EmployeeDAO {
	

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

}
