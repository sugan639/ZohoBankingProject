//package com.sbank.netbanking.auth;
//
//import java.io.IOException;
//import java.util.stream.Collectors;
//
//import javax.servlet.Filter;
//import javax.servlet.FilterChain;
//import javax.servlet.ServletException;
//import javax.servlet.ServletRequest;
//import javax.servlet.ServletResponse;
//import javax.servlet.annotation.WebFilter;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import com.sbank.netbanking.dao.ClientBankDAO;
//import com.sbank.netbanking.exceptions.TaskException;
//import com.sbank.netbanking.handler.model.ClientData;
//import com.sbank.netbanking.util.HMACUtil;
//
//
//@WebFilter("/interbank/transfer")
//public class HMACAuthFilter implements Filter {
//
//    @Override
//    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
//            throws IOException, ServletException {
//        
//        HttpServletRequest request = (HttpServletRequest) req;
//        HttpServletResponse response = (HttpServletResponse) res;
//
//        String ifsc = request.getHeader("X-IFSC");
//        String receivedSignature = request.getHeader("X-Signature");
//
//        if (ifsc == null || receivedSignature == null) {
//            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//            response.getWriter().write("Missing headers");
//            return;
//        }
//        
//        try {
//        ClientData clientData = fetchSecretKeyFromDB(ifsc);  // Lookup in client_bank table
//        String secretKey = clientData.getSecretKey();
//        
//        if (secretKey == null) {
//            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//            response.getWriter().write("Invalid IFSC");
//            return;
//        }
//
//        // Read request body as string
//        String body = getRequestBody(request);
//
//        // Compute HMAC
//        String computedSignature = HMACUtil.generateHMAC(body, secretKey);
//        if (!computedSignature.equals(receivedSignature)) {
//            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//            response.getWriter().write("Invalid signature");
//            return;
//        }
//        
//        req.setAttribute("clientData", clientData);
//
//        // Passed HMAC check
//        chain.doFilter(req, res);
//        }
//        catch(TaskException e) {
//        	e.printStackTrace();
//        }
//    }
//
//    private ClientData fetchSecretKeyFromDB(String bankCode) throws TaskException {
//    	ClientBankDAO clientBankDAO = new ClientBankDAO();
//        ClientData clientData = clientBankDAO.getClientBankData(bankCode); // Uses first 4 letters
//        if (clientData != null) {
//            return clientData;
//        } else {
//            return null;
//        }
//    }
//
//    private String getRequestBody(HttpServletRequest request) throws IOException {
//        return request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
//    }
//}
