package com.sbank.netbanking.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.sbank.netbanking.auth.CookieEncryptor;
import com.sbank.netbanking.constants.AppConstants;
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
import com.sbank.netbanking.util.HMACUtil;
import com.sbank.netbanking.util.PojoJsonConverter;

public class InterBankHandler {

    public void interbankCredit(HttpServletRequest req, HttpServletResponse res) throws TaskException, IOException {
        BufferedReader reader = null;
        String requestBody = "";

        try {
            // 1. Read raw request body (needed for HMAC verification)
            StringBuilder sb = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(req.getInputStream(), "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            requestBody = sb.toString();

            if (requestBody.isEmpty()) {
                ErrorResponseUtil.send(res, HttpServletResponse.SC_BAD_REQUEST,
                    new ErrorResponse("Bad Request", 400, "Empty request body"));
                return;
            }

            // 2. Get headers
            String senderIfsc = req.getHeader("X-IFSC");
            String receivedSignature = req.getHeader("X-Signature");

            System.out.println("Received X-IFSC: " + senderIfsc);
            System.out.println("Received X-Signature: " + receivedSignature);
            System.out.println("Request Body: " + requestBody);

            // 3. Validate required headers
            if (senderIfsc == null || senderIfsc.trim().isEmpty() ||
                receivedSignature == null || receivedSignature.trim().isEmpty()) {
                sendError(res, HttpServletResponse.SC_UNAUTHORIZED, "Missing X-IFSC or X-Signature header");
                return;
            }

            // 4. Fetch sender bank's secret key from DB using IFSC
            ClientBankDAO clientBankDAO = new ClientBankDAO();
            ClientData clientData = clientBankDAO.getClientBankData(senderIfsc.trim());

            if (clientData == null || clientData.getSecretKey() == null) {
                sendError(res, HttpServletResponse.SC_UNAUTHORIZED, "Invalid or unknown IFSC code");
                return;
            }

            String encryptedSecretKey = clientData.getSecretKey();
            // Decrypting secret key
            CookieEncryptor encryptor = new CookieEncryptor();
            String secretKey = encryptor.decrypt(encryptedSecretKey);

            // 5. Compute expected HMAC from request body
            String expectedSignature = HMACUtil.generateHMAC(requestBody, secretKey);

            // 6. Constant-time comparison to prevent timing attacks
            if (!secureEquals(receivedSignature.trim(), expectedSignature)) {
                System.out.println("HMAC Mismatch!");
                System.out.println("Expected: " + expectedSignature);
                System.out.println("Received: " + receivedSignature);
                sendError(res, HttpServletResponse.SC_UNAUTHORIZED, "HMAC validation failed");
                return;
            }

            // 7. Parse JSON after HMAC passes
            JSONObject json = new JSONObject(requestBody); // Use already-read body

            Long fromAccount = json.optLong("from_account", -1);
            Long toAccount = json.optLong("to_account", -1);
            Double amount = json.optDouble("amount", -1);
            String transactionType = json.optString("transaction_type", "");            
            
            System.out.println("Danger here at interbankHandler: "+fromAccount+ " : " +toAccount+" : " + amount);
            // 8. Validate payload fields
            if (fromAccount <= 0 || toAccount <= 0 || amount <= 0 ||
                !"INTER_BANK_CREDIT".equalsIgnoreCase(transactionType)
               ) { // assuming ZOHO is expected
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
            long transactionId = System.currentTimeMillis(); // or use your generator

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
            String yourBankSecret = "your-bank-shared-secret"; // Should be fetched securely
            String responseSignature = HMACUtil.generateHMAC(responseBody, yourBankSecret);

            res.setHeader("X-IFSC", "YOURBANK");         // Your bank's IFSC
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
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

        public JSONObject initiateInterbankTransfer(Long fromAccount,
									                Long toAccount,
									                Double amount,
									                long userId,
									                long transactionId,
									                String ifscCode) throws TaskException {


        	 
            ClientBankDAO clientBankDAO = new ClientBankDAO();


            // 2. Get destination bank details
            ClientData clientData = clientBankDAO.getClientBankData(ifscCode);
            if (clientData == null || clientData.getClientUrl() == null || clientData.getSecretKey() == null) {
                rollbackDebit(fromAccount, amount, userId, transactionId, toAccount, ifscCode);
                JSONObject resp = new JSONObject();
                resp.put("success", false);
                resp.put("message", "Destination bank not found in system");
                return resp;
            }

            HttpURLConnection conn = null;
            try {
                // 3. Prepare payload
                JSONObject payload = new JSONObject();
                payload.put("from_account", fromAccount);
                payload.put("to_account", toAccount);
                payload.put("amount", amount);
                payload.put("request_type", "MONEYTRANSFER");
                payload.put("transaction_type", "INTER_BANK_CREDIT");
                payload.put("transaction_id", transactionId);
                payload.put("sender_ifsc_code", AppConstants.MY_IFSC);

                String requestBody = payload.toString();
                String encryptedSecretKey = clientData.getSecretKey();
                // Decrypting secret key
                CookieEncryptor encryptor = new CookieEncryptor();
                String secretKey = encryptor.decrypt(encryptedSecretKey);
                String signature = HMACUtil.generateHMAC(requestBody, secretKey);

                // 4. Call external bank
                URL url = new URL(clientData.getClientUrl());
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("X-IFSC", AppConstants.MY_IFSC);
                conn.setRequestProperty("X-Signature", signature);
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

                String responseStr = responseBody.toString().trim();
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
                JSONObject success = new JSONObject();
                success.put("success", true);
                success.put("external_response", externalResp);
                success.put("http_code", statusCode);
                return success;

            } catch (Exception e) {
                e.printStackTrace();
                rollbackDebit(fromAccount, amount, userId, transactionId, toAccount, ifscCode);
                JSONObject error = new JSONObject();
                error.put("success", false);
                error.put("message", "Network error or timeout");
                error.put("detail", e.getMessage());
                return error;
            } finally {
                if (conn != null) conn.disconnect();
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
    

    // Secure string comparison to prevent timing attacks
    private boolean secureEquals(String s1, String s2) {
        if (s1 == null || s2 == null) return false;
        if (s1.length() != s2.length()) return false;
        int result = 0;
        for (int i = 0; i < s1.length(); i++) {
            result |= s1.charAt(i) ^ s2.charAt(i);
        }
        return result == 0;
    }

    // Helper to send error without throwing extra exceptions
    private void sendError(HttpServletResponse res, int status, String message) throws IOException {
        res.setStatus(status);
        res.setContentType("application/json");
        JSONObject error = new JSONObject();
        error.put("error", message);
        res.getWriter().write(error.toString());
    }
}