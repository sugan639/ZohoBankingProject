package com.sbank.netbanking.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.sbank.netbanking.dbconfig.ConnectionManager;
import com.sbank.netbanking.exceptions.ExceptionMessages;
import com.sbank.netbanking.exceptions.TaskException;
import com.sbank.netbanking.model.User;
import com.sbank.netbanking.model.User.Role;

public class UserDAO {

    public User getUserById(long userId) throws TaskException {
    	
        String sql = "SELECT * FROM users WHERE user_id = ?";
        
        ConnectionManager connectionManager = new ConnectionManager();
        connectionManager.initConnection();
        Connection conn = connectionManager.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)){
        	
        	 stmt.setLong(1, userId);
           

           try( ResultSet rs = stmt.executeQuery()){

        	
        	
            if (rs.next()) {
                User user = new User();
                user.setUserId(rs.getLong("user_id"));
                user.setName(rs.getString("name"));
                user.setPassword(rs.getString("password")); // For comparison
                user.setEmail(rs.getString("email"));
                user.setMobileNumber(rs.getLong("mobile_number"));
                user.setRole(Role.valueOf(rs.getString("role")));
                user.setCreatedAt(rs.getLong("created_at"));
                user.setModifiedAt(rs.getLong("modified_at"));
                user.setModifiedBy(rs.getLong("modified_by"));
                return user;
            } 
            
            else {
                return null;
            }
           }

        }
        
        catch (SQLException e) {
        	
            throw new TaskException(ExceptionMessages.USERDATA_RETRIEVAL_FAILED, e);
        } 
        finally {
           
            connectionManager.stopConnection();
        }
    }
}
