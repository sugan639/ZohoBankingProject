package com.sbank.netbanking.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.sbank.netbanking.dbconfig.ConnectionManager;
import com.sbank.netbanking.exceptions.TaskException;
import com.sbank.netbanking.handler.model.ClientData;

public class ClientBankDAO {

    public ClientData getClientBankData(String ifsc) throws TaskException {
    	
    	  if (ifsc == null || ifsc.length() < 4) {
    	        throw new TaskException("Invalid IFSC code: " + ifsc);
    	    }
        String query = "SELECT ifsc_code, bank_name, client_url, secret_key FROM client_bank WHERE ifsc_code = ?";
        
        String bankCode = ifsc.substring(0, 4);
                
        try (ConnectionManager cm = new ConnectionManager()) {
            cm.initConnection();
            Connection conn = cm.getConnection();

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, bankCode);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                ClientData client = new ClientData();
                client.setIfscCode(rs.getString("ifsc_code"));
                client.setBankName(rs.getString("bank_name"));
                client.setClientUrl(rs.getString("client_url"));
                client.setPublicKey(rs.getString("secret_key"));
                return client;
            }
         }

        } catch (SQLException e) {
        	
            e.printStackTrace(); // or log it
	        throw new TaskException("Unable to get client bank data:  " + ifsc);

        }
        catch (Exception e) {
            throw new TaskException("Database connection error", e);
        }

        return null;
    }
    

    public boolean addClientBank(ClientData clientData) throws TaskException {
        String query = "INSERT INTO client_bank (ifsc_code, bank_name, client_url, secret_key) VALUES (?, ?, ?, ?)";

        try (ConnectionManager cm = new ConnectionManager()) {
            cm.initConnection();
            Connection conn = cm.getConnection();

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, clientData.getIfscCode());
                stmt.setString(2, clientData.getBankName());
                stmt.setString(3, clientData.getClientUrl());
                stmt.setString(4, clientData.getPublicKey());

                int rows = stmt.executeUpdate();
                return rows > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new TaskException("Failed to add client bank: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new TaskException("Database connection error", e);
        }
    }
    
    public Boolean existsByIfsc(String ifscCode) throws TaskException  {
    	
        String query = "SELECT 1 FROM client_bank WHERE ifsc_code = ?";
        try (ConnectionManager cm = new ConnectionManager()) {
            cm.initConnection();
            Connection conn = cm.getConnection();

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, ifscCode);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException  e) {
            throw new TaskException("Database error checking IFSC", e);
        }
        catch (Exception e) {
            throw new TaskException("Database connection error", e);
        }
    }

}
