package com.sbank.netbanking.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.sbank.netbanking.dbconfig.ConnectionManager;
import com.sbank.netbanking.exceptions.TaskException;
import com.sbank.netbanking.model.Account;
import com.sbank.netbanking.model.Account.AccountStatus;

public class AccountDAO {

    public Account getAccountByNumber(long accountNumber) throws TaskException {
        String sql = "SELECT * FROM accounts WHERE account_number = ?";
        try (ConnectionManager cm = new ConnectionManager()) {
            cm.initConnection();
            Connection conn = cm.getConnection();

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, accountNumber);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Account account = new Account();
                        account.setAccountNumber(rs.getLong("account_number"));
                        account.setUserId(rs.getLong("user_id"));
                        account.setBalance(rs.getDouble("balance"));
                        account.setBranchId(rs.getLong("branch_id"));
                        account.setStatus(AccountStatus.valueOf(rs.getString("status")));
                        account.setCreatedAt(rs.getLong("created_at"));
                        account.setModifiedAt(rs.getLong("modified_at"));
                        account.setModifiedBy(rs.getLong("modified_by"));
                        return account;
                    }
                }
            }
        } catch (SQLException e) {
            throw new TaskException("Failed to fetch account", e);
        } catch (Exception e) {
            throw new TaskException("Database connection error", e);
        }
        return null;
    }

    public void updateBalance(long accountNumber, double newBalance, long modifiedBy) throws TaskException {
        String sql = "UPDATE accounts SET balance = ?, modified_at = ?, modified_by = ? WHERE account_number = ?";
        long modifiedAt = System.currentTimeMillis();
        try (ConnectionManager cm = new ConnectionManager()) {
            cm.initConnection();
            Connection conn = cm.getConnection();

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setDouble(1, newBalance);
                stmt.setLong(2, modifiedAt);
                stmt.setLong(3, modifiedBy);
                stmt.setLong(4, accountNumber);
                stmt.executeUpdate();
            }

        } catch (SQLException e) {
            throw new TaskException("Failed to update account balance", e);
        } catch (Exception e) {
            throw new TaskException("Database connection error", e);
        }
    }
}
