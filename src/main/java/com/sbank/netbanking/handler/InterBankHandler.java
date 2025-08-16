package com.sbank.netbanking.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.sbank.netbanking.constants.AppConstants;
import com.sbank.netbanking.crypt.SignatureUtil;
import com.sbank.netbanking.dao.AccountDAO;
import com.sbank.netbanking.dao.ClientBankDAO;
import com.sbank.netbanking.dao.TransactionDAO;
import com.sbank.netbanking.dto.ErrorResponse;
import com.sbank.netbanking.exceptions.TaskException;
import com.sbank.netbanking.handler.model.ClientData;
import com.sbank.netbanking.model.Account;
import com.sbank.netbanking.model.Transaction;
import com.sbank.netbanking.model.Transaction.TransactionType;
import com.sbank.netbanking.util.ErrorResponseUtil;
import com.sbank.netbanking.util.PojoJsonConverter;
import com.sbank.netbanking.util.RequestJsonConverter;
import com.sbank.netbanking.util.TransactionUtil;

public class InterBankHandler { 

    public void interbankCredit(HttpServletRequest req, HttpServletResponse res) throws TaskException, IOException { 
    	RequestJsonConverter jsonConverter = new RequestJsonConverter();


        try {
            JSONObject json = jsonConverter.convertToJson(req);

            String requestExpiryDurationString = json.optString("expiry_duration");
            Long requestExpiryDuration = Long.parseLong(requestExpiryDurationString);
            
            String requestBody = json.toString();
            
            // 2. Get headers
            String senderIfsc = req.getHeader("X-IFSC");
            String receivedSignature = req.getHeader("X-Signature");
            String requestTimestampString = req.getHeader("X-Timestamp");
            Long requestTimeStamp = Long.parseLong(requestTimestampString);

            // 3. Validate required headers
            if (senderIfsc == null || senderIfsc.trim().isEmpty() || requestTimeStamp ==null|| requestExpiryDuration==null ||
                receivedSignature == null || receivedSignature.trim().isEmpty()) {
             
                ErrorResponseUtil.send(res, HttpServletResponse.SC_UNAUTHORIZED,
  	                    new ErrorResponse("Request Expired", 409, "Missing X-IFSC or X-Signature header"));
  	                return;
            }
            
            long expiryTimeMillis =requestTimeStamp +requestExpiryDuration ;
            long currentTimeMillis = System.currentTimeMillis();
            if(currentTimeMillis >expiryTimeMillis ) {
            	  ErrorResponseUtil.send(res, HttpServletResponse.SC_CONFLICT,
  	                    new ErrorResponse("Request Expired", 409, "Request expired, plese send new request"));
  	                return;
            }
        
            // Decrypting signature key
            ClientBankDAO clientBankDao = new ClientBankDAO();
            ClientData  clientData =  clientBankDao.getClientBankData(senderIfsc);
            String pbKey = clientData.getPublicKey();
            
            PublicKey publicKey = loadPublicKey(pbKey);
            // 5. Compute expected HMAC from request body
            SignatureUtil signatureUtil = new SignatureUtil();
            Boolean signature = signatureUtil.verify(requestBody,receivedSignature, publicKey);   
            
            // 6. Constant-time comparison to prevent timing attacks
            if (!signature) {
           	  ErrorResponseUtil.send(res, HttpServletResponse.SC_CONFLICT,
	                    new ErrorResponse("Request Expired", 401, "Signature validation failed"));
	                return;
            
            }

            // 7. Parse JSON after HMAC passes

            Long fromAccount = json.optLong("from_account", -1);
            Long toAccount = json.optLong("to_account", -1);
            Double amount = json.optDouble("amount", -1);
            String transactionType = json.optString("transaction_type", "");            
            
            // 8. Validate payload fields
            if (fromAccount <= 0 || toAccount <= 0 || amount <= 0 ||
                !"INTER_BANK_CREDIT".equalsIgnoreCase(transactionType)
               ) { 
                sendError(res, HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid or missing fields in request body");
                return;
            }

            // 9. Validate recipient account
            AccountDAO accountDAO = new AccountDAO();
            Account recipient = accountDAO.getAccountByNumber(toAccount);

            if (recipient == null) {
                sendError(res, HttpServletResponse.SC_BAD_REQUEST, "Recipient account not found");
                return;
            }

            if (!"ACTIVE".equalsIgnoreCase(recipient.getStatus().name())) {
                sendError(res, HttpServletResponse.SC_BAD_REQUEST, "Recipient account is not active");
                return;
            }

            // 10. Perform credit
            TransactionDAO transactionDAO = new TransactionDAO();
    		TransactionUtil transactionUtil = new TransactionUtil();
			long transactionId = transactionUtil.generateTransactionId();// or use your generator  ===============================================

            Transaction creditTxn = transactionDAO.deposit(
                toAccount,
                amount,
                0L, // system user ID
                TransactionType.INTERBANK_CREDIT,
                transactionId,
                fromAccount,
                senderIfsc // sender bank IFSC
            );

            // 11. Prepare success response
            PojoJsonConverter pojoConverter = new PojoJsonConverter();
            JSONObject responseJson = new JSONObject();
            responseJson.put("status", "SUCCESS");
            responseJson.put("message", "Interbank credit successful");
            responseJson.put("transaction_id", transactionId);
            responseJson.put("transaction", pojoConverter.pojoToJson(creditTxn));

            String responseBody = responseJson.toString();

            // 12. Sign the response with YOUR bank's secret (optional, but recommended)
            // Assuming your bank's IFSC is "YOURBANK" and you have a fixed secret for outgoing responses
            PrivateKey privateKey = getPrivateKeyFromEnv();
            String responseSignature = signatureUtil.encrypt(responseBody, privateKey);

            res.setHeader("X-IFSC", AppConstants.MY_IFSC);         // Your bank's IFSC
            res.setHeader("X-Signature", responseSignature);

            // 13. Send response
            res.setContentType("application/json");
            res.setStatus(HttpServletResponse.SC_OK);
            res.getWriter().write(responseBody);

        } catch (IOException e) {
            e.printStackTrace();
            sendError(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to read request");
        } catch (Exception e) {
            e.printStackTrace();
            sendError(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal processing error");
        } 
    }

    
    
    
    
    
    
    
    
    
    
    
    
        public JSONObject initiateInterbankTransfer(Long fromAccount,
									                Long toAccount,
									                Double amount,
									                long userId,
									                long transactionId,
									                String ifscCode) throws TaskException {


        	 
            ClientBankDAO clientBankDAO = new ClientBankDAO();
            SignatureUtil signatureUtil = new SignatureUtil();
            JSONObject response = new JSONObject();
            try {
            // 2. Get destination bank details
            ClientData clientData = clientBankDAO.getClientBankData(ifscCode);
            PrivateKey privateKey = getPrivateKeyFromEnv();

            if (clientData == null || clientData.getClientUrl() == null) {
                rollbackDebit(fromAccount, amount, userId, transactionId, toAccount, ifscCode);
                JSONObject resp = new JSONObject();
                resp.put("success", false);
                resp.put("message", "Destination bank not found in system");
                return resp;
            }

            HttpURLConnection conn = null;

 
                // 3. Prepare payload
            	Long currentTime = System.currentTimeMillis();
            	String timeStamp = currentTime.toString();
                JSONObject payload = new JSONObject();
                payload.put("from_account", fromAccount);
                payload.put("to_account", toAccount);
                payload.put("amount", amount);
                payload.put("request_type", "MONEYTRANSFER");
                payload.put("transaction_type", "INTER_BANK_CREDIT");
                payload.put("transaction_id", transactionId);
                payload.put("sender_ifsc_code", AppConstants.MY_IFSC);
                payload.put("expiry_duration", AppConstants.REQUEST_TIMEOUT_MILLIS); // 5 Minutes

                String requestBody = payload.toString();
                          
                String signature = signatureUtil.encrypt(requestBody, privateKey); // Work pending to implement this
                // 4. Call external bank
                URL url = new URL(clientData.getClientUrl());
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("X-IFSC", AppConstants.MY_IFSC);
                conn.setRequestProperty("X-Signature", signature);
                conn.setRequestProperty("X-Timestamp", timeStamp);
                conn.setDoOutput(true);
                conn.setConnectTimeout(10_000);
                conn.setReadTimeout(30_000);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(requestBody.getBytes(StandardCharsets.UTF_8));
                }
                
                // 5. Read response
                int statusCode = conn.getResponseCode();
                StringBuilder responseBody = new StringBuilder();
                InputStream stream = (statusCode >= 400) ? conn.getErrorStream() : conn.getInputStream();
                
                if (stream != null) {
                    try (BufferedReader br = new BufferedReader(
                            new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            responseBody.append(line);
                        }
                    }
                }

                String responseStr = responseBody.toString().trim();	// Need to verify the response signature
                JSONObject externalResp;
                try {
                    externalResp = responseStr.isEmpty() ? new JSONObject() : new JSONObject(responseStr);
                } catch (Exception e) {
                    externalResp = new JSONObject();
                    externalResp.put("error", "Invalid JSON from external bank");
                    externalResp.put("raw_response", responseStr);
                }

                String status = externalResp.optString("status", "").trim().toUpperCase();

                if (statusCode != 200 || !"SUCCESS".equals(status)) {
                    rollbackDebit(fromAccount, amount, userId, transactionId, toAccount, ifscCode);
                    JSONObject error = new JSONObject();
                    error.put("success", false);
                    error.put("message", "Transfer failed at destination bank");
                    error.put("external_status", status);
                    error.put("http_code", statusCode);
                    error.put("external_response", externalResp);
                    return error;
                }

                // 6. Success
                response.put("success", true);
                response.put("external_response", externalResp);
                response.put("http_code", statusCode);
                return response;

            } catch (Exception e) {
                e.printStackTrace();
                rollbackDebit(fromAccount, amount, userId, transactionId, toAccount, ifscCode);
                JSONObject error = new JSONObject();
                error.put("success", false);
                error.put("message", "Network error or timeout");
                error.put("detail", e.getMessage());
                return error;
            } 
        }

        private void rollbackDebit(Long fromAccount, Double amount, long userId, long transactionId,
                                   Long toAccount, String ifscCode) {
            try {
                final TransactionDAO transactionDAO = new TransactionDAO();

                transactionDAO.deposit(
                        fromAccount, amount, userId,
                        Transaction.TransactionType.INTERBANK_REFUND,
                        transactionId, toAccount, ifscCode);
            } catch (Exception ex) {
            	ex.printStackTrace();
            }
        }



    // Helper to send error without throwing extra exceptions
    private void sendError(HttpServletResponse res, int status, String message) throws IOException {
        res.setStatus(status);
        res.setContentType("application/json");
        JSONObject error = new JSONObject();
        error.put("error", message);
        res.getWriter().write(error.toString());
    }
    
    public PrivateKey getPrivateKeyFromEnv() throws Exception {
        String base64Key = System.getenv("privateKey");
        if (base64Key == null || base64Key.trim().isEmpty()) {
            throw new IllegalStateException("PRIVATE_KEY environment variable is not set");
        }

        // Clean the key: remove whitespace, newlines, tabs
        String cleanedKey = base64Key
            .replaceAll("\\s", ""); // Removes all whitespace, including \n, \r, spaces

        if (cleanedKey.isEmpty()) {
            throw new IllegalArgumentException("Private key is empty after cleaning");
        }

        // Decode Base64
        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(cleanedKey);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Base64 encoding in private key", e);
        }

        // Generate private key
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }
    
    

    
    public static PublicKey loadPublicKey(String base64PublicKey) throws Exception {
        // 1. Decode the Base64 string to bytes
        byte[] decodedKey = Base64.getDecoder().decode(base64PublicKey);

        // 2. Create key spec (X.509 for public keys)
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);

        // 3. Generate PublicKey object
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }
    
    
    
}



















