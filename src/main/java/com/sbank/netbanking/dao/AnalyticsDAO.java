package com.sbank.netbanking.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sbank.netbanking.dbconfig.ConnectionManager;
import com.sbank.netbanking.exceptions.ExceptionMessages;
import com.sbank.netbanking.exceptions.TaskException;
import com.sbank.netbanking.handler.model.AccountActivity;
import com.sbank.netbanking.model.BranchCustomerBalance;

public class AnalyticsDAO {

    public Map<String, Double> getMonthlyDepositWithdrawal() throws TaskException {
        Map<String, Double> result = new HashMap<>();

        LocalDate now = LocalDate.now();
        long startOfMonth = now.withDayOfMonth(1)
                .atStartOfDay()
                .toInstant(ZoneOffset.UTC)
                .toEpochMilli();
        long endOfMonth = now.plusMonths(1)
                .withDayOfMonth(1)
                .atStartOfDay()
                .toInstant(ZoneOffset.UTC)
                .toEpochMilli() - 1;

        String sql = "SELECT type, SUM(amount) AS total FROM transactions " +
                "WHERE timestamp BETWEEN ? AND ? AND type IN ('DEPOSIT','WITHDRAWAL') GROUP BY type";

        try (ConnectionManager cm = new ConnectionManager()) {
            cm.initConnection();
            Connection conn = cm.getConnection();

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, startOfMonth);
                ps.setLong(2, endOfMonth);

                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String type = rs.getString("type");
                    double total = rs.getDouble("total");
                    result.put(type, total);
                }
            }
        } catch (SQLException e) {
            throw new TaskException("Failed to fetch monthly totals", e);
        } catch (Exception e) {
            throw new TaskException(ExceptionMessages.DATABASE_CONNECTION_FAILED, e);
        }

        return result;
    }

    public List<AccountActivity> getTopActiveAccounts(int limit, int pastDays) throws TaskException {
        List<AccountActivity> list = new ArrayList<>();
        long fromTS = System.currentTimeMillis() - pastDays * 86_400_000L;

        String sql = "SELECT account_number, COUNT(*) AS txn_count FROM transactions " +
                "WHERE timestamp >= ? GROUP BY account_number ORDER BY txn_count DESC LIMIT ?";

        try (ConnectionManager cm = new ConnectionManager()) {
            cm.initConnection();
            Connection conn = cm.getConnection();

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, fromTS);
                ps.setInt(2, limit);

                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    long accNum = rs.getLong("account_number");
                    long count = rs.getLong("txn_count");
                    AccountActivity activity = new AccountActivity();
                    activity.setAccountNumber(accNum);
                    activity.setTxnCount(count);
                    list.add(activity);
                }
            }
        } catch (SQLException e) {
            throw new TaskException("Failed to fetch active accounts", e);
        } catch (Exception e) {
            throw new TaskException(ExceptionMessages.DATABASE_CONNECTION_FAILED, e);
        }

        return list;
    }
    
    
    /* ───────────────────────────────────────────────
    Branch Transaction Summary (DEPOSIT / WITHDRAW /
    TRANSFER  for given branch & time‑range)
 ─────────────────────────────────────────────── */
 public Map<String, Double> getBranchTxnSummary(long branchId,
                                                long fromTs,
                                                long toTs) throws TaskException {

     Map<String, Double> map = new HashMap<>();

     String sql = "SELECT type, SUM(amount) AS total " +
                  "FROM transactions t " +
                  "JOIN accounts a ON t.account_number = a.account_number " +
                  "WHERE a.branch_id = ? " +
                  "  AND t.timestamp BETWEEN ? AND ? " +
                  "  AND type IN ('DEPOSIT','WITHDRAWAL','INTRA_BANK_DEBIT','INTRA_BANK_CREDIT','INTERBANK_DEBIT','INTERBANK_CREDIT') " +
                  "GROUP BY type";

     try (ConnectionManager cm = new ConnectionManager()) {
         cm.initConnection();
         Connection conn = cm.getConnection();

         try (PreparedStatement ps = conn.prepareStatement(sql)) {
             ps.setLong(1, branchId);
             ps.setLong(2, fromTs);
             ps.setLong(3, toTs);

             ResultSet rs = ps.executeQuery();
             while (rs.next()) {
                 map.put(rs.getString("type"), rs.getDouble("total"));
             }
         }
     } catch (SQLException e) {
         throw new TaskException("Failed to fetch branch summary", e);
     } catch (Exception e) {
         throw new TaskException(ExceptionMessages.DATABASE_CONNECTION_FAILED, e);
     }

     return map;
 }

 /* ───────────────────────────────────────────────
    Top customers in branch by balance
 ─────────────────────────────────────────────── */
 public List<BranchCustomerBalance> getTopCustomers(long branchId,
                                                    int limit) throws TaskException {

     List<BranchCustomerBalance> list = new ArrayList<>();

     String sql = "SELECT u.name, a.account_number, a.balance " +
                  "FROM accounts a " +
                  "JOIN users u ON a.user_id = u.user_id " +
                  "WHERE a.branch_id = ? " +
                  "ORDER BY a.balance DESC " +
                  "LIMIT ?";

     try (ConnectionManager cm = new ConnectionManager()) {
         cm.initConnection();
         Connection conn = cm.getConnection();

         try (PreparedStatement ps = conn.prepareStatement(sql)) {
             ps.setLong(1, branchId);
             ps.setInt(2, limit);

             ResultSet rs = ps.executeQuery();
             while (rs.next()) {
                 BranchCustomerBalance b = new BranchCustomerBalance();
                 b.setName(rs.getString("name"));
                 b.setAccountNumber(rs.getLong("account_number"));
                 b.setBalance(rs.getDouble("balance"));
                 list.add(b);
             }
         }
     } catch (SQLException e) {
         throw new TaskException("Failed to fetch top customers", e);
     } catch (Exception e) {
         throw new TaskException(ExceptionMessages.DATABASE_CONNECTION_FAILED, e);
     }

     return list;
 }
		
		 /* Helper to get start‑of‑day epoch millis (UTC) */
		 public long startOfToday() {
		     return LocalDate.now()
		             .atStartOfDay()
		             .toInstant(ZoneOffset.UTC)
		             .toEpochMilli();
		 }
		
		 /* Helper to get first day of month epoch millis (UTC) */
		 public long startOfMonth() {
		     return LocalDate.now()
		             .withDayOfMonth(1)
		             .atStartOfDay()
		             .toInstant(ZoneOffset.UTC)
		             .toEpochMilli();
		 }
}
