package com.sbank.netbanking.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.sbank.netbanking.dbconfig.ConnectionManager;
import com.sbank.netbanking.exceptions.TaskException;
import com.sbank.netbanking.model.Beneficiary;

public class BeneficiaryDAO {
	

    public long addBeneficiary(Beneficiary beneficiary) throws TaskException {
        String sql = "INSERT INTO beneficiery (customer_id, beneficiery_account_number, beneficiery_name, "
                   + "beneficiery_ifsc_code, created_at, modified_at, modified_by) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (ConnectionManager cm = new ConnectionManager()) {
            cm.initConnection();
            Connection conn = cm.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setLong(1, beneficiary.getCustomerId());
                stmt.setLong(2, beneficiary.getBeneficiaryAccountNumber());
                stmt.setString(3, beneficiary.getBeneficiaryName());
                stmt.setString(4, beneficiary.getBeneficiaryIfscCode());
                stmt.setLong(5, beneficiary.getCreatedAt());
                stmt.setLong(6, beneficiary.getModifiedAt());
                stmt.setLong(7, beneficiary.getModifiedBy());

                int rows = stmt.executeUpdate();
                if (rows == 0) {
                    throw new TaskException("Failed to insert beneficiary");
                }

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getLong(1);
                    } else {
                        throw new TaskException("Failed to fetch beneficiary ID");
                    }
                }

            }
        } catch (SQLException e) {
            throw new TaskException("Database error while adding beneficiary", e);
        } catch (Exception e) {
            throw new TaskException("Unexpected error while adding beneficiary", e);
        }
    }
    
    public List<Beneficiary> getBeneficiariesByCustomerId(long customerId) throws TaskException {
        String sql = "SELECT * FROM beneficiery WHERE customer_id = ?";
        List<Beneficiary> list = new ArrayList<>();

        try (ConnectionManager cm = new ConnectionManager()) {
            cm.initConnection();
            Connection conn = cm.getConnection();

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, customerId);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    Beneficiary b = new Beneficiary();
                    b.setBeneficiaryUniqueNumber(rs.getLong("beneficiery_unique_number"));
                    b.setCustomerId(rs.getLong("customer_id"));
                    b.setBeneficiaryAccountNumber(rs.getLong("beneficiery_account_number"));
                    b.setBeneficiaryName(rs.getString("beneficiery_name"));
                    b.setBeneficiaryIfscCode(rs.getString("beneficiery_ifsc_code"));
                    b.setCreatedAt(rs.getLong("created_at"));
                    b.setModifiedAt(rs.getLong("modified_at"));
                    b.setModifiedBy(rs.getLong("modified_by"));
                    list.add(b);
                }
            }
        } catch (SQLException e) {
            throw new TaskException("Failed to fetch beneficiaries", e);
        } catch (Exception e) {
            throw new TaskException("Unexpected error while fetching beneficiaries", e);
        }

        return list;
    }

}
