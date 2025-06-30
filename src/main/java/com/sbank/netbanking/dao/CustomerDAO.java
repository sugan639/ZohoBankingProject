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
import com.sbank.netbanking.model.Customer;
import com.sbank.netbanking.model.NewCustomer;

public class CustomerDAO {
	

	
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

    public Customer getCustomerById(long userId) throws TaskException {
        String sql = "SELECT u.user_id, u.name, u.email, u.mobile_number, u.role, " +
                     "u.created_at, u.modified_at, u.modified_by, " +
                     "c.customer_id, c.dob, c.address, c.aadhar_number, c.pan_number " +
                     "FROM users u INNER JOIN customers c ON u.user_id = c.customer_id " +
                     "WHERE u.user_id = ?";

        try (ConnectionManager connectionManager = new ConnectionManager()) {
            connectionManager.initConnection();
            Connection conn = connectionManager.getConnection();

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, userId);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Customer customer = new Customer();
                        customer.setCustomerId(rs.getLong("user_id"));
                        customer.setName(rs.getString("name"));
                        customer.setEmail(rs.getString("email"));
                        customer.setMobileNumber(rs.getLong("mobile_number"));
                        customer.setRole(rs.getString("role"));
                        customer.setCreatedAt(rs.getLong("created_at"));
                        customer.setModifiedAt(rs.getLong("modified_at"));
                        customer.setModifiedBy(rs.getLong("modified_by"));
                        customer.setDob(rs.getLong("dob"));
                        customer.setAddress(rs.getString("address"));
                        customer.setAadharNumber(rs.getLong("aadhar_number"));
                        customer.setPanNumber(rs.getString("pan_number"));
                        return customer;
                    }
                }

            } catch (SQLException e) {
                throw new TaskException("Failed to fetch customer by ID", e);
            }

        } catch (Exception e) {
            throw new TaskException(ExceptionMessages.DATABASE_CONNECTION_FAILED, e);
        }

        return null; // no match found
    }

    
    
    
    
    
    
    public void updateCustomerFields(Long customerId, Long dob, String address, Long aadhar, String pan) throws TaskException {
        if (dob == null && address == null && aadhar == null && pan == null) {
            // No fields to update in customers table
            return;
        }

        StringBuilder sql = new StringBuilder("UPDATE customers SET ");
        List<Object> params = new ArrayList<>();

        if (dob != null) {
            sql.append("dob = ?, ");
            params.add(dob);
        }
        if (address != null) {
            sql.append("address = ?, ");
            params.add(address);
        }
        if (aadhar != null) {
            sql.append("aadhar_number = ?, ");
            params.add(aadhar);
        }
        if (pan != null) {
            sql.append("pan_number = ?, ");
            params.add(pan);
        }

        // Remove last comma and space
        if (sql.toString().endsWith(", ")) {
            sql.setLength(sql.length() - 2);
        }

        sql.append(" WHERE customer_id = ?");
        params.add(customerId);

        try (ConnectionManager connectionManager = new ConnectionManager()) {
            connectionManager.initConnection();
            Connection conn = connectionManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    stmt.setObject(i + 1, params.get(i));
                }
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new TaskException("Failed to update customer data", e);
        } catch (Exception e) {
            throw new TaskException(ExceptionMessages.DATABASE_CONNECTION_FAILED, e);
        }
    }
    
    public void updateCustomerProfileFields(Long customerId, Long dob, String address) throws TaskException {
        StringBuilder sql = new StringBuilder("UPDATE customers SET ");
        List<Object> params = new ArrayList<>();

        if (dob != null) {
            sql.append("dob = ?, ");
            params.add(dob);
        }
        if (address != null) {
            sql.append("address = ?, ");
            params.add(address);
        }

        if (params.isEmpty()) return; // No update needed

        // Trim the last comma and add WHERE clause
        sql.setLength(sql.length() - 2);
        sql.append(" WHERE customer_id = ?");
        params.add(customerId);

        try (ConnectionManager connectionManager = new ConnectionManager()) {
            connectionManager.initConnection();
            Connection conn = connectionManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    stmt.setObject(i + 1, params.get(i));
                }
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new TaskException("Failed to update customer profile", e);
        } catch (Exception e) {
            throw new TaskException(ExceptionMessages.DATABASE_CONNECTION_FAILED, e);
        }
    }
    
    

    


}
