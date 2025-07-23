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
import com.sbank.netbanking.model.User;
import com.sbank.netbanking.model.User.Role;

public class UserDAO {
	
	 ConnectionManager connectionManager = new ConnectionManager();

    public User getUserById(long userId) throws TaskException {
    	
        String sql = "SELECT * FROM users WHERE user_id = ?";

        
        try (ConnectionManager connectionManager = new ConnectionManager()){
        		connectionManager.initConnection();
                Connection conn = connectionManager.getConnection();  
                
        	try(PreparedStatement stmt = conn.prepareStatement(sql)){
        	
        		
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
        	}
        
        catch (SQLException e) {
        	
            throw new TaskException(ExceptionMessages.USERDATA_RETRIEVAL_FAILED, e);
        }
        catch (Exception e) {
        	
            throw new TaskException(ExceptionMessages.USERDATA_RETRIEVAL_FAILED, e);
        } 

    }
    
    public void updateUserFields(Long userId, String name, String email, Long mobileNumber, Long modifiedBy, String password) throws TaskException {
        if (name == null && email == null && mobileNumber == null) {
            // Nothing meaningful to update
            return;
        }

        StringBuilder sql = new StringBuilder("UPDATE users SET ");
        List<Object> params = new ArrayList<>();

        if (name != null) {
            sql.append("name = ?, ");
            params.add(name);
        }
        if (email != null) {
            sql.append("email = ?, ");
            params.add(email);
        }
        if (mobileNumber != null) {
            sql.append("mobile_number = ?, ");
            params.add(mobileNumber);
        }
        
        if(password!= null) {
      	  sql.append("password = ?, ");
            params.add(password);
      }


        sql.append("modified_at = ?, modified_by = ? WHERE user_id = ?");
        params.add(System.currentTimeMillis());
        params.add(modifiedBy);
        params.add(userId);

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
            throw new TaskException("Failed to update user data", e);
        } catch (Exception e) {
            throw new TaskException(ExceptionMessages.USERDATA_UPDATE_FAILED, e);
        }
    }



    
    
    
}
