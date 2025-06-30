package com.sbank.netbanking.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.sbank.netbanking.dbconfig.ConnectionManager;
import com.sbank.netbanking.exceptions.ExceptionMessages;
import com.sbank.netbanking.exceptions.TaskException;
import com.sbank.netbanking.model.Employee;
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
		                emp.setCreatedAt(currentTime);
		                emp.setModifiedAt(currentTime);
		                emp.setModifiedBy(createdBy);

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

		
		

}
