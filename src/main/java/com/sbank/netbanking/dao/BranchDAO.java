package com.sbank.netbanking.dao;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.sbank.netbanking.dbconfig.ConnectionManager;
import com.sbank.netbanking.exceptions.ExceptionMessages;
import com.sbank.netbanking.exceptions.TaskException;
import com.sbank.netbanking.model.Branch;


public class BranchDAO {

    public Branch getBranchById(long branchId) throws TaskException {
        String sql = "SELECT * FROM branches WHERE branch_id = ?";
        
        try (ConnectionManager connectionManager = new ConnectionManager()){
    		connectionManager.initConnection();
            Connection conn = connectionManager.getConnection();  
            
    	try(PreparedStatement stmt = conn.prepareStatement(sql)){

            stmt.setLong(1, branchId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Branch branch = new Branch();
                    branch.setBranchId(rs.getLong("branch_id"));
                    branch.setAdminId(rs.getLong("admin_id"));
                    branch.setIfscCode(rs.getString("ifsc_code"));
                    branch.setBankName(rs.getString("bank_name"));
                    branch.setLocation(rs.getString("location"));
                    branch.setCreatedAt(rs.getLong("created_at"));
                    branch.setModifiedAt(rs.getLong("modified_at"));
                    branch.setModifiedBy(rs.getLong("modified_by"));
                    return branch;
                }
            }

        } catch (SQLException e) {
            throw new TaskException("Failed to fetch branch with ID " + branchId, e);
        }

        return null;
    }
        
        catch (Exception e) {
            throw new TaskException(ExceptionMessages.SESSION_ID_RETRIEVAL_FAILED, e);
        }
    
}
}
