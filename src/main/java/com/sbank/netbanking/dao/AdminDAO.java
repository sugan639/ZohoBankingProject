package com.sbank.netbanking.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.sbank.netbanking.dbconfig.ConnectionManager;
import com.sbank.netbanking.exceptions.TaskException;
import com.sbank.netbanking.model.Admin;
import com.sbank.netbanking.model.User.Role;

public class AdminDAO {

	public Admin getAdminById(long employeeId) throws TaskException {
            String sql = "SELECT u.user_id, u.name, u.email, u.mobile_number, u.role, " +
                         "u.created_at, u.modified_at, u.modified_by, e.branch_id " +
                         "FROM users u JOIN employees e ON u.user_id = e.employee_id " +
                         "WHERE u.user_id = ?";

            
            try (ConnectionManager connectionManager = new ConnectionManager()){
            		connectionManager.initConnection();
                    Connection conn = connectionManager.getConnection();  
                    
            	try(PreparedStatement stmt = conn.prepareStatement(sql)){
            	
            		
                stmt.setLong(1, employeeId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Admin admin = new Admin();
                        admin.setEmployeeId(rs.getLong("user_id")); // same as employee_id
                        admin.setName(rs.getString("name"));
                        admin.setEmail(rs.getString("email"));
                        admin.setMobileNumber(rs.getLong("mobile_number"));
                        admin.setRole(Role.valueOf(rs.getString("role")));

                        admin.setBranchId(rs.getLong("branch_id"));
                        admin.setCreatedAt(rs.getLong("created_at"));
                        admin.setModifiedAt(rs.getLong("modified_at"));
                        admin.setModifiedBy(rs.getLong("modified_by"));
                        return admin;
                    } else {
                        return null;
                    }
                }
            } catch (SQLException e) {
                throw new TaskException("Error fetching admin data", e);
            }
    } catch (Exception e) {
		// TODO Auto-generated catch block
        throw new TaskException("Error fetching admin data", e);
	}
	}
    
    

   
    
}
