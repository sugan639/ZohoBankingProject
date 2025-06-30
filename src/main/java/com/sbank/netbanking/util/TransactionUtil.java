package com.sbank.netbanking.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.sbank.netbanking.dbconfig.ConnectionManager;
import com.sbank.netbanking.exceptions.ExceptionMessages;
import com.sbank.netbanking.exceptions.TaskException;

public class TransactionUtil {

	public long generateTransactionId() throws SQLException, TaskException {
	    long newId = 0;
	    String getLastTransactionId = "SELECT last_id FROM transaction_id_tracker FOR UPDATE";
	    String updateLastTransactionId = "UPDATE transaction_id_tracker SET last_id = ?";

	    
	    try (ConnectionManager cm = new ConnectionManager()) {
            cm.initConnection();
            Connection conn = cm.getConnection();

	    try (
	        PreparedStatement selectStmt = conn.prepareStatement(getLastTransactionId);
	        PreparedStatement updateStmt = conn.prepareStatement(updateLastTransactionId)
	    ) {
	        conn.setAutoCommit(false);  // Begin transaction

	        ResultSet rs = selectStmt.executeQuery();
	        if (rs.next()) {
	            long lastId = rs.getLong("last_id");
	            newId = lastId + 1;

	            updateStmt.setLong(1, newId);
	            updateStmt.executeUpdate();

	            conn.commit();
	            conn.setAutoCommit(true); // Restore autocommit
	            return newId;
	        } else {
	            conn.rollback();
	            throw new TaskException("Transaction ID tracker not initialized");
	        }
	    } catch (SQLException e) {
	        conn.rollback();
	        throw new TaskException("Error generating transaction_id", e);
	    }
	    }
	    catch (Exception e) {
	        throw new TaskException(ExceptionMessages.WRONG_ACTION, e);
	    }
	}

}
