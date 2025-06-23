package com.sbank.netbanking.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.sbank.netbanking.dbconfig.ConnectionManager;
import com.sbank.netbanking.exceptions.ExceptionMessages;
import com.sbank.netbanking.exceptions.TaskException;
import com.sbank.netbanking.model.SessionData;

public class SessionDAO {
	
	
	public SessionData getSessionData(String sessionId) throws TaskException { // work pending
	      
		SessionData sessionData =  new SessionData();					// Data carrying POJO
        String sql = "SELECT * FROM session WHERE session_id = ?";		
        
        try (PreparedStatement pstmt = prepareStatement(sql)) {
            pstmt.setString(1, sessionId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
            		
                    sessionData.setUserId(rs.getLong("user_id"));
                    sessionData.setSessionID(rs.getString("session_id"));
                    sessionData.setStartTime(rs.getLong("start_time"));
                    sessionData.setExpiryDuration(rs.getLong("expiry_duration"));
                    
                }
            }
        }
        
        catch (SQLException e) {
            throw new TaskException(ExceptionMessages.SESSION_ID_RETRIEVAL_FAILED, e);
        }
    
	
	return sessionData;
	}
	
	
	public void createDbSession(SessionData sessionData) throws TaskException {
		String sql = "INSERT INTO session (session_id, user_id, start_time, expiry_duration) " +
                "VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE session_id = VALUES(session_id), start_time = VALUES(start_time), expiry_duration = VALUES(expiry_duration)";

   try (PreparedStatement pstmt = prepareStatement(sql)) {
       pstmt.setString(1, sessionData.getSessionID());
       pstmt.setLong(2, sessionData.getUserId());
       pstmt.setLong(3, sessionData.getStartTime());
       pstmt.setLong(4, sessionData.getExpiryDuration());

       pstmt.executeUpdate();
   } catch (SQLException e) {
       throw new TaskException("Failed to insert/update session", e);
   }
	}
	
	
	public boolean deleteSessionBySessionId(String sessionId) throws TaskException {
	    String query = "DELETE FROM session WHERE session_id = ?";
	    
	    try (PreparedStatement stmt = prepareStatement(query)) {

	        stmt.setString(1, sessionId);
	        int rowsAffected = stmt.executeUpdate();
	        
	        System.out.println(rowsAffected);

	        return rowsAffected > 0; // true if session was deleted

	    } catch (SQLException e) {
	        throw new TaskException(ExceptionMessages.SESSION_DATA_DELETION_FAILED + sessionId, e);
	    }
	}

	// To Update time in session
	public boolean updateSessionStartTime(String sessionId, long newStartTime) throws TaskException {
	    String sql = "UPDATE session SET start_time = ? WHERE session_id = ?";
	    
	    try (PreparedStatement pstmt = prepareStatement(sql)) {
	        pstmt.setLong(1, newStartTime);
	        pstmt.setString(2, sessionId);
	        
	        int rows = pstmt.executeUpdate();
	        return rows > 0;
	        
	    } catch (SQLException e) {
	        throw new TaskException("Failed to update session start time", e);
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
