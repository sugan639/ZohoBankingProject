package com.sbank.netbanking.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.sbank.netbanking.dbconfig.ConnectionManager;
import com.sbank.netbanking.exceptions.ExceptionMessages;
import com.sbank.netbanking.exceptions.TaskException;
import com.sbank.netbanking.model.SessionData;
import com.sbank.netbanking.model.User.Role;

public class SessionDAO {
	
	
	public SessionData getSessionData(long userId) throws TaskException {
	      
		SessionData sessionData =  new SessionData();					// Data carrying POJO
        String sql = "SELECT * FROM session WHERE user_id = ?";		
        
        try (PreparedStatement pstmt = prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
            		
                    sessionData.setUserId(rs.getLong("user_id"));
                    sessionData.setSessionID(rs.getString("session_id"));
                    sessionData.setRole(Role.valueOf(rs.getString("role")));
                    sessionData.setStartTime(rs.getLong("start_time"));
                    
                }
            }
        }
        
        catch (SQLException e) {
            throw new TaskException(ExceptionMessages.SESSION_ID_RETRIEVAL_FAILED, e);
        }
    
	
	return sessionData;
	}
	
	public void createDbSession(SessionData sessionData) throws TaskException {
	    String sql = "INSERT INTO session (session_id, user_id, role, start_time) VALUES (?, ?, ?, ?)";

	    try (PreparedStatement pstmt = prepareStatement(sql)) {
	        pstmt.setString(1, sessionData.getSessionID());
	        pstmt.setLong(2, sessionData.getUserId());
	        pstmt.setString(3, sessionData.getRole().name());
	        pstmt.setLong(4, sessionData.getStartTime());

	        int rowsInserted = pstmt.executeUpdate();
	        if (rowsInserted == 0) {
	            throw new TaskException("Failed to create session. No rows inserted.");
	        }
	    } catch (SQLException e) {
	        throw new TaskException(ExceptionMessages.SESSION_DATA_INSERT_FAILED, e);
	    }
	}

	

	

    // Prepare a statement
    private PreparedStatement prepareStatement(String sql) throws SQLException, TaskException {

   	 	ConnectionManager connectionManager = new ConnectionManager();

        connectionManager.initConnection();
        Connection conn = connectionManager.getConnection();
        return conn.prepareStatement(sql);
    }
	

}
