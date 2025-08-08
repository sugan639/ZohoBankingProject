package com.sbank.netbanking.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.sbank.netbanking.dbconfig.ConnectionManager;
import com.sbank.netbanking.handler.model.ClientData;

public class ClientBankDAO {

    public ClientData getClientBankData(String bankCode) {
        String query = "SELECT ifsc_code, bank_name, client_url, secret_key FROM client_bank WHERE ifsc_code = ?";
        
        try (Connection connection = new ConnectionManager().getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            
            stmt.setString(1, bankCode);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                ClientData client = new ClientData();
                client.setIfscCode(rs.getString("ifsc_code"));
                client.setBankName(rs.getString("bank_name"));
                client.setClientUrl(rs.getString("client_url"));
                client.setSecretKey(rs.getString("secret_key"));
                return client;
            }

        } catch (SQLException e) {
            e.printStackTrace(); // or log it
        }

        return null;
    }
}
