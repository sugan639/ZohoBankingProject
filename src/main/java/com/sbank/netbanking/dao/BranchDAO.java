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
    
    public Branch updateBranch(Long branchId, Long adminId, String ifscCode, String bankName, String location) throws TaskException {
        if (branchId == null ) {
            throw new TaskException("branchId  required");
        }

        StringBuilder sql = new StringBuilder("UPDATE branches SET ");
        List<Object> params = new ArrayList<>();

        // Dynamically add only non-null fields
        if (adminId != null) {
            sql.append("admin_id = ?, ");
            params.add(adminId);
        }
        if (ifscCode != null) {
            sql.append("ifsc_code = ?, ");
            params.add(ifscCode);
        }
        if (bankName != null) {
            sql.append("bank_name = ?, ");
            params.add(bankName);
        }
        if (location != null) {
            sql.append("location = ?, ");
            params.add(location);
        }

        // Always include audit fields
        sql.append("modified_at = ?, modified_by = ? WHERE branch_id = ?");
        long currentTime = System.currentTimeMillis();
        params.add(currentTime);
        params.add(adminId);
        params.add(branchId);

        if (params.isEmpty()) {
            throw new TaskException("No fields provided for update");
        }

        try (ConnectionManager connectionManager = new ConnectionManager()) {
            connectionManager.initConnection();
            Connection conn = connectionManager.getConnection();

            try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    stmt.setObject(i + 1, params.get(i));
                }

                int rows = stmt.executeUpdate();
                if (rows > 0) {
                	System.out.println("Branch Update success! ");
                    return getBranchById(branchId);
                } else {
                    return null;
                }
            } catch (SQLException e) {
                throw new TaskException("Failed to update branch: " + e.getMessage(), e);
            }

        } catch (Exception e) {
            throw new TaskException(ExceptionMessages.SESSION_ID_RETRIEVAL_FAILED, e);
        }
    }


}









