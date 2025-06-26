package com.sbank.netbanking.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.sbank.netbanking.dbconfig.ConnectionManager;
import com.sbank.netbanking.exceptions.ExceptionMessages;
import com.sbank.netbanking.exceptions.TaskException;
import com.sbank.netbanking.model.Customer;

public class CustomerDAO {

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


}
