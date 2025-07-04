package com.sbank.netbanking.handler;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.sbank.netbanking.dao.CustomerDAO;
import com.sbank.netbanking.dto.ErrorResponse;
import com.sbank.netbanking.exceptions.TaskException;
import com.sbank.netbanking.model.NewCustomer;
import com.sbank.netbanking.service.BcryptService;
import com.sbank.netbanking.util.DateUtil;
import com.sbank.netbanking.util.ErrorResponseUtil;
import com.sbank.netbanking.util.PojoJsonConverter;
import com.sbank.netbanking.util.RequestJsonConverter;

public class NewUserRegister {
	
	public void registerUser(HttpServletRequest req, HttpServletResponse res) throws TaskException {

		RequestJsonConverter jsonConverter = new RequestJsonConverter();
        PojoJsonConverter pojoConverter = new PojoJsonConverter();
        CustomerDAO customerDAO = new CustomerDAO();
		
        try {
		  // Extract JSON from request
        JSONObject json = jsonConverter.convertToJson(req);


        // Required fields from request
        String name = json.optString("name", null);
        String password = json.optString("password", null);

        String email = json.optString("email", null);
        String dobString = json.optString("dob", null); // format: yyyy-MM-dd
        String address = json.optString("address", null);
        Long mobileNumber = json.has("mobile_number") ? json.getLong("mobile_number") : null;
        Long aadharNumber = json.has("aadhar_number") ? json.getLong("aadhar_number") : null;
        String panNumber = json.optString("pan_number", null);
        String role = "CUSTOMER";
        
        long createdBy = 0;		// Here 0 means self registration

        // Generate random password

        String hashedPassword = BcryptService.hashPassword(password);
        
        if (name == null || password == null || email == null || dobString == null || address == null || 
            mobileNumber == null || aadharNumber == null || panNumber == null) {
            ErrorResponseUtil.send(res, HttpServletResponse.SC_BAD_REQUEST,
                new ErrorResponse("Bad Request", 400, "All fields are required"));
            return;
        }

        // Convert DOB to epoch millis
        long dobMillis = DateUtil.convertDateToEpoch(dobString);

        // Save to DB
        NewCustomer customer = customerDAO.addNewCustomer(name, hashedPassword, email, mobileNumber, dobMillis,
                                                    address, aadharNumber, panNumber, createdBy, role);

        // Convert response
        customer.setPassword(password);  // setting plain new password for new customer
        JSONObject jsonResponse = pojoConverter.pojoToJson(customer);
        jsonResponse.put("message", "New user registered successfully");

        res.setContentType("application/json");
        res.getWriter().write(jsonResponse.toString());

    } catch (IOException e) {
    	e.printStackTrace();
        ErrorResponseUtil.send(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            new ErrorResponse("TaskException", 500, e.getMessage()));
    }
      catch(Exception e) {
    		e.printStackTrace();
            ErrorResponseUtil.send(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                new ErrorResponse("TaskException", 500, e.getMessage()));
      }
        
}
}	

