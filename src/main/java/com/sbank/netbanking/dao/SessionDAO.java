package com.sbank.netbanking.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.sbank.netbanking.dbconfig.ConnectionManager;
import com.sbank.netbanking.exceptions.ExceptionMessages;
import com.sbank.netbanking.exceptions.TaskException;
import com.sbank.netbanking.model.SessionData;

public class SessionDAO {
	
	
	public SessionData getSessionData(Long sessionId) throws TaskException { 
	      
		SessionData sessionData =  new SessionData();					// Data carrying POJO
        String sql = "SELECT * FROM session WHERE id = ?";		
        
        try (ConnectionManager connectionManager = new ConnectionManager()){
    		connectionManager.initConnection();
            Connection conn = connectionManager.getConnection();  
            
    	try(PreparedStatement pstmt = conn.prepareStatement(sql)){
    	
    		
            pstmt.setLong(1, sessionId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
            		
                    sessionData.setUserId(rs.getLong("user_id"));
                    sessionData.setSessionID(rs.getString("session_id"));
                    sessionData.setStartTime(rs.getLong("start_time"));
                    sessionData.setExpiryDuration(rs.getLong("expiry_duration"));
                    sessionData.setId(rs.getLong("id"));
                    
                }
            }
        }
        }
        
        catch (Exception e) {
            throw new TaskException(ExceptionMessages.SESSION_ID_RETRIEVAL_FAILED, e);
        }
    
	
	return sessionData;
	}
	
	
	
	public long createDbSession(SessionData sessionData) throws TaskException {
		String sql = "INSERT INTO session (session_id, user_id, start_time, expiry_duration) " +
                "VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE session_id = VALUES(session_id), start_time = VALUES(start_time), expiry_duration = VALUES(expiry_duration)";

        try (ConnectionManager connectionManager = new ConnectionManager()){
    		connectionManager.initConnection();
            Connection conn = connectionManager.getConnection();  
            
    	try(PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
    	
    		
       pstmt.setString(1, sessionData.getSessionID());
       pstmt.setLong(2, sessionData.getUserId());
       pstmt.setLong(3, sessionData.getStartTime());
       pstmt.setLong(4, sessionData.getExpiryDuration());

       pstmt.executeUpdate();
 
       try (ResultSet rs = pstmt.getGeneratedKeys()) {
           if (rs.next()) {
               return rs.getLong(1); // Return the generated ID
           }
       }

    	}
    	
    	return 0;

        }
        catch (Exception e) {
       throw new TaskException("Failed to insert/update session", e);
   }
	}
	
	
	public boolean deleteSessionBySessionId(Long id) throws TaskException {
	    String sql = "DELETE FROM session WHERE id = ?";
	    
        try (ConnectionManager connectionManager = new ConnectionManager()){
    		connectionManager.initConnection();
            Connection conn = connectionManager.getConnection();  
            
    	try(PreparedStatement stmt = conn.prepareStatement(sql)){
    	
	        stmt.setLong(1, id);
	        int rowsAffected = stmt.executeUpdate();
	        
	        return rowsAffected > 0; // true if session was deleted

	    	} 
        }
        
	catch (Exception e) {
        throw new TaskException(ExceptionMessages.SESSION_DATA_DELETION_FAILED + id, e);
    }
        
}
	
	

	// To Update time in session
	public boolean updateSessionStartTime(long id, long newStartTime) throws TaskException {
	    String sql = "UPDATE session SET start_time = ? WHERE id = ?";
	    
        try (ConnectionManager connectionManager = new ConnectionManager()){
    		connectionManager.initConnection();
            Connection conn = connectionManager.getConnection();  
            
    	try(PreparedStatement pstmt = conn.prepareStatement(sql)){
    	
    		
	        pstmt.setLong(1, newStartTime);
	        pstmt.setLong(2, id);
	        
	        int rows = pstmt.executeUpdate();
	        return rows > 0;
	        
	    } 
    	}

        catch (Exception e) {
	        throw new TaskException("Failed to update session start time", e);
	    }
	}

	
	public List<SessionData> checkSessionByUserId(Long userId) throws TaskException {
	    
	    List<SessionData> sessions = new ArrayList<>();
	    String sql = "SELECT * FROM session WHERE user_id = ?";        

	    try (ConnectionManager connectionManager = new ConnectionManager()) {
	        connectionManager.initConnection();
	        Connection conn = connectionManager.getConnection();  

	        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
	            pstmt.setLong(1, userId);

	            try (ResultSet rs = pstmt.executeQuery()) {
	                while (rs.next()) {
	                    SessionData sessionData = new SessionData();
	                    sessionData.setUserId(rs.getLong("user_id"));
	                    sessionData.setSessionID(rs.getString("session_id"));
	                    sessionData.setStartTime(rs.getLong("start_time"));
	                    sessionData.setExpiryDuration(rs.getLong("expiry_duration"));
	                    sessionData.setId(rs.getLong("id"));

	                    sessions.add(sessionData);
	                }
	            }
	        }
	    } catch (Exception e) {
	        throw new TaskException(ExceptionMessages.SESSION_ID_RETRIEVAL_FAILED, e);
	    }

	    return sessions;
	}

	
	public int clearAllUserSession(Long userId , Long sessionId ) throws TaskException {
	    String sql = "DELETE FROM session WHERE user_id = ? AND id != ?";

	    try (ConnectionManager connectionManager = new ConnectionManager()){
	        connectionManager.initConnection();
	        Connection conn = connectionManager.getConnection();  

	        try(PreparedStatement stmt = conn.prepareStatement(sql)){
	            stmt.setLong(1, userId);      // Example: 100000039
	            stmt.setLong(2, sessionId);   // Example: 23

	            int rowsAffected = stmt.executeUpdate();
	            System.out.println("Deleted sessions: " + rowsAffected);
	            return rowsAffected;

	        } 
	    } catch (Exception e) {
	        throw new TaskException(ExceptionMessages.SESSION_DATA_DELETION_FAILED, e);
	    }
	}

	

	
	
}
	













