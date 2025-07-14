package com.sbank.netbanking.handler;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sbank.netbanking.dao.AccountDAO;
import com.sbank.netbanking.dao.CustomerDAO;
import com.sbank.netbanking.dao.EmployeeDAO;
import com.sbank.netbanking.dao.TransactionDAO;
import com.sbank.netbanking.dao.UserDAO;
import com.sbank.netbanking.dto.ErrorResponse;
import com.sbank.netbanking.dto.UserDTO;
import com.sbank.netbanking.exceptions.TaskException;
import com.sbank.netbanking.model.Account;
import com.sbank.netbanking.model.Customer;
import com.sbank.netbanking.model.Employee;
import com.sbank.netbanking.model.NewCustomer;
import com.sbank.netbanking.model.SessionData;
import com.sbank.netbanking.model.Transaction;
import com.sbank.netbanking.model.Transaction.TransactionType;
import com.sbank.netbanking.model.User;
import com.sbank.netbanking.model.User.Role;
import com.sbank.netbanking.service.BcryptService;
import com.sbank.netbanking.service.TransactionService;
import com.sbank.netbanking.util.DateUtil;
import com.sbank.netbanking.util.ErrorResponseUtil;
import com.sbank.netbanking.util.PojoJsonConverter;
import com.sbank.netbanking.util.RandomPasswordGenerator;
import com.sbank.netbanking.util.RequestJsonConverter;
import com.sbank.netbanking.util.TransactionUtil;
import com.sbank.netbanking.util.UserMapper;

public class EmployeeHandler {

	// GET /employee/profile/{user_id}
	public void getProfile(HttpServletRequest req, HttpServletResponse res) throws TaskException{


		
	    SessionData sessionData =  new SessionData();
	    sessionData = (SessionData) req.getAttribute("sessionData");
        long employeeId = sessionData.getUserId();
      
        System.out.println("User ID from the session data get profile method: "+employeeId);
        EmployeeDAO employeeDao = new EmployeeDAO();
        PojoJsonConverter converter = new PojoJsonConverter();
        
        

        try {
	    	//Authorization 
	
	    	
	    	 UserDAO userDAO = new UserDAO();
	            Role authRole = userDAO.getUserById(employeeId).getRole();
	            if(authRole != Role.EMPLOYEE) {
	            	 ErrorResponseUtil.send(res, HttpServletResponse.SC_UNAUTHORIZED,
	                         new ErrorResponse("Unauthorized", 403, "Permission Denied"));
	            	 return;
	            }
	            
        	Employee admin = employeeDao.getEmployeeById(employeeId);
            if (admin != null) {
                JSONObject jsonAdmin = converter.pojoToJson(admin);
                res.setContentType("application/json");
                res.getWriter().write(jsonAdmin.toString());
            } else {
                res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                res.getWriter().write("{\"error\":\"Admin not found\", \"code\":404}");
            }
        } catch (IOException e) {
        	  ErrorResponseUtil.send(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
      	            new ErrorResponse("TaskException", 500, e.getMessage()));
        }
        
	}
	

    // GET /employee/users/{user_id}
    public void getUser(HttpServletRequest req, HttpServletResponse res) throws TaskException {
    	//Authorization 
	    SessionData sessionData =  new SessionData();
	    sessionData = (SessionData) req.getAttribute("sessionData");
        long employeeId = sessionData.getUserId();
    	
    	 UserDAO userDao = new UserDAO();
    	 User userAuth = userDao.getUserById(employeeId);
            Role authRole = userAuth.getRole();
            if(authRole != Role.EMPLOYEE) {
            	 ErrorResponseUtil.send(res, HttpServletResponse.SC_UNAUTHORIZED,
                         new ErrorResponse("Unauthorized", 403, "Permission Denied"));
            	 return;
            }
            
    	 String userIdParam = req.getParameter("user_id");

 	    long userId;

 	    try {
 	        userId = Long.parseLong(userIdParam);
 	    } catch (NumberFormatException e) {
 	        ErrorResponseUtil.send(res, HttpServletResponse.SC_BAD_REQUEST,
 	            new ErrorResponse("Bad Request", 400, "Invalid user_id format"));
 	        return;
 	    }
 	    
 	    

 	    UserDAO userDAO = new UserDAO(); // Assumed
 	    PojoJsonConverter converter = new PojoJsonConverter();

 	    try {
 	        User user = userDAO.getUserById(userId);		// User object to get the role
             UserMapper userMapper = new UserMapper();
             UserDTO userDataDto = userMapper.toUserDTO(user);	// DTO to remove the password field
             
 	        if (userDataDto == null) {
 	            ErrorResponseUtil.send(res, HttpServletResponse.SC_NOT_FOUND,
 	                new ErrorResponse("Not Found", 404, "User not found"));
 	            return;
 	        }

 	        switch (userDataDto.getRole()) {
 	        case CUSTOMER:
 	            CustomerDAO customerDAO = new CustomerDAO();
 	            Customer customer = customerDAO.getCustomerById(userId);
 	            if (customer != null) {
 	                JSONObject custJson = converter.pojoToJson(customer);
 	                res.setContentType("application/json");
 	    	        res.getWriter().write(custJson.toString());	            }
 	            break;

 	        case EMPLOYEE:
 	        case ADMIN:
 	        	EmployeeDAO employeeDAO = new EmployeeDAO();
 	            Employee employee = employeeDAO.getEmployeeById(userId);
 	            if (employee != null) {
 	                JSONObject empJson = converter.pojoToJson(employee);
 	                res.setContentType("application/json");
 	    	        res.getWriter().write(empJson.toString());
 	    	        }
 	            break;
 	    }


 	    } catch (IOException e) {
 	        ErrorResponseUtil.send(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
 	            new ErrorResponse("Server Error", 500, e.getMessage()));
 	    }
    }


    // POST /employee/customers
    public void addCustomer(HttpServletRequest req, HttpServletResponse res) throws TaskException {
    	//Authorization 
	    SessionData sessionData =  new SessionData();
	    sessionData = (SessionData) req.getAttribute("sessionData");
        long employeeId = sessionData.getUserId();
    	
    	 UserDAO userDao = new UserDAO();
            Role authRole = userDao.getUserById(employeeId).getRole();
            if(authRole != Role.EMPLOYEE) {
            	 ErrorResponseUtil.send(res, HttpServletResponse.SC_UNAUTHORIZED,
                         new ErrorResponse("Unauthorized", 403, "Permission Denied"));
            	 return;
            }
        
    	RequestJsonConverter jsonConverter = new RequestJsonConverter();
        PojoJsonConverter pojoConverter = new PojoJsonConverter();
        CustomerDAO customerDAO = new CustomerDAO();
        RandomPasswordGenerator passwordGenerator = new RandomPasswordGenerator();

        try {
            // Extract JSON from request
            JSONObject json = jsonConverter.convertToJson(req);

            // Get session data (to know who created the customer)
            long createdBy = sessionData.getUserId();

            // Required fields from request
            String name = json.optString("name", null);
            String email = json.optString("email", null);
            String dobString = json.optString("dob", null); // format: yyyy-MM-dd
            String address = json.optString("address", null);
            Long mobileNumber = json.has("mobile_number") ? json.getLong("mobile_number") : null;
            Long aadharNumber = json.has("aadhar_number") ? json.getLong("aadhar_number") : null;
            String panNumber = json.optString("pan_number", null);
            String role = "CUSTOMER";

            // Generate random password
            String password = passwordGenerator.generateRandomPassword(8);

            String hashedPassword = BcryptService.hashPassword(password);
            
            if (name == null || email == null || dobString == null || address == null || 
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
            jsonResponse.put("message", "Customer and user added successfully");

            res.setContentType("application/json");
            res.getWriter().write(jsonResponse.toString());

        } catch (IOException e) {
        	e.printStackTrace();
            ErrorResponseUtil.send(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                new ErrorResponse("TaskException", 500, e.getMessage()));
        }
    }
    

    // POST /employee/account
    public void createAccount(HttpServletRequest req, HttpServletResponse res) throws TaskException {
    	//Authorization 
	    SessionData sessionData =  new SessionData();
	    sessionData = (SessionData) req.getAttribute("sessionData");
        long employeeId = sessionData.getUserId();
    	
    	 UserDAO userDao = new UserDAO();
            Role authRole = userDao.getUserById(employeeId).getRole();
            if(authRole != Role.EMPLOYEE) {
            	 ErrorResponseUtil.send(res, HttpServletResponse.SC_UNAUTHORIZED,
                         new ErrorResponse("Unauthorized", 403, "Permission Denied"));
            	 return;
            }
    	RequestJsonConverter jsonConverter = new RequestJsonConverter();
        PojoJsonConverter pojoConverter = new PojoJsonConverter();
        UserDAO userDAO = new UserDAO();

        try {
            JSONObject json = jsonConverter.convertToJson(req);

            Long userId = json.has("user_id") ? json.getLong("user_id") : null;
            Long branchId = json.has("branch_id") ? json.getLong("branch_id") : null;
            Double initialBalance = json.has("balance") ? json.getDouble("balance") : 0.0;

            if (userId == null || branchId == null) {
                ErrorResponseUtil.send(res, HttpServletResponse.SC_BAD_REQUEST,
                    new ErrorResponse("Bad Request", 400, "user_id and branch_id are required"));
                return;
            }
            
	        //Employee can perform operations on his branch only
 	       // Get employee branch_id
	          EmployeeDAO employeeDAO = new EmployeeDAO();
	          Employee employee = employeeDAO.getEmployeeById(employeeId);
	          long employeeBranchId = employee.getBranchId();

	        
	          AccountDAO accountDAO = new AccountDAO();
	  

	          if (employeeBranchId != branchId) {
	              ErrorResponseUtil.send(res, HttpServletResponse.SC_FORBIDDEN,
	                      new ErrorResponse("Unauthorized", 403, "You can only create accounts for your branch"));
	              return;
	          }

            // Validate user is a customer
            User user = userDAO.getUserById(userId);
            if (user == null || !user.getRole().name().equals("CUSTOMER")) {
                ErrorResponseUtil.send(res, HttpServletResponse.SC_BAD_REQUEST,
                    new ErrorResponse("Bad Request", 400, "Account can only be created for customers"));
                return;
            }

            // Get admin ID from session (created_by, modified_by)
            long createdBy = sessionData.getUserId();

            // Create the account
            Account account = accountDAO.createCustomerAccount(userId, branchId, initialBalance, createdBy);

            // Return created account
            JSONObject jsonResponse = pojoConverter.pojoToJson(account);
            jsonResponse.put("message", "Account created successfully");

            res.setContentType("application/json");
            res.getWriter().write(jsonResponse.toString());

        } catch (IOException e) {
            ErrorResponseUtil.send(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                new ErrorResponse("TaskException", 500, e.getMessage()));
        }
    }


    // GET /employee/transactions
    public void getTransactions(HttpServletRequest req, HttpServletResponse res) throws TaskException {
        try {
            SessionData sessionData = (SessionData) req.getAttribute("sessionData");
            long empId = sessionData.getUserId();

            UserDAO userDAO = new UserDAO();
            if (userDAO.getUserById(empId).getRole() != Role.EMPLOYEE) {
                throw new TaskException("Unauthorized");
            }

            RequestJsonConverter converter = new RequestJsonConverter();
            JSONObject json = converter.convertToJson(req);

            TransactionService transactionService = new TransactionService();
            JSONObject result = transactionService.fetchTransactions(json, Role.EMPLOYEE, empId);

            res.setContentType("application/json");
            res.getWriter().write(result.toString());

        } catch (Exception e) {
            e.printStackTrace();
            ErrorResponseUtil.send(res, 500, new ErrorResponse("Error", 500, e.getMessage()));
        }
    }



    // PUT /employee/users/{user_id}
    public void updateCustomer(HttpServletRequest req, HttpServletResponse res) throws TaskException {
    	//Authorization 
	    SessionData sessionData =  new SessionData();
	    sessionData = (SessionData) req.getAttribute("sessionData");
        long employeeId = sessionData.getUserId();
    	
    	 UserDAO userDao = new UserDAO();
            Role authRole = userDao.getUserById(employeeId).getRole();
            if(authRole != Role.EMPLOYEE) {
            	 ErrorResponseUtil.send(res, HttpServletResponse.SC_UNAUTHORIZED,
                         new ErrorResponse("Unauthorized", 403, "Permission Denied"));
            	 return;
            }
            
            
    	RequestJsonConverter jsonConverter = new RequestJsonConverter();
	    UserDAO userDAO = new UserDAO();
	    CustomerDAO customerDAO = new CustomerDAO();

	    try {
	        JSONObject json = jsonConverter.convertToJson(req);

	        if (json == null || json.isEmpty()) {
	            ErrorResponseUtil.send(res, HttpServletResponse.SC_BAD_REQUEST,
	                new ErrorResponse("Bad Request", 400, "Request body is empty"));
	            return;
	        }

	     

	        Long userId = json.has("user_id") ? json.getLong("user_id") : null;
	        if (userId == null) {
	            ErrorResponseUtil.send(res, HttpServletResponse.SC_BAD_REQUEST,
	                new ErrorResponse("Bad Request", 400, "user_id is required"));
	            return;
	        }
	        
	        

	        long modifiedBy = sessionData.getUserId();

	        // Fetch role
	        User user = userDAO.getUserById(userId);
	        if (user == null) {
	            ErrorResponseUtil.send(res, HttpServletResponse.SC_NOT_FOUND,
	                new ErrorResponse("Not Found", 404, "User not found"));
	            return;
	        }

	        String role = user.getRole().name();
	        
	        if(role!=Role.CUSTOMER.name()) {
	        	   ErrorResponseUtil.send(res, HttpServletResponse.SC_BAD_REQUEST,
		                    new ErrorResponse("Bad Request", 400, "Employee can only update customer data!"));
		                return;
	        }

	        // Common fields
	        String name = json.optString("name", null);
	        String email = json.optString("email", null);
	        Long mobileNumber = json.has("mobile_number") ? json.getLong("mobile_number") : null;

	        // Update users table
	        userDAO.updateUserFields(userId, name, email, mobileNumber, modifiedBy);

	        // Role-based updates
	        if (role.equals("CUSTOMER")) {
	            // Validate that no employee-specific fields are present
	            if (json.has("branch_id")) {
	                ErrorResponseUtil.send(res, HttpServletResponse.SC_BAD_REQUEST,
	                    new ErrorResponse("Bad Request", 400, "Customers should not have branch_id"));
	                return;
	            }

	            // Customer-specific fields
	            String dobStr = json.optString("dob", null);
	            Long dob = dobStr != null ? DateUtil.convertDateToEpoch(dobStr) : null;
	            String address = json.optString("address", null);
	            Long aadhar = json.has("aadhar_number") ? json.getLong("aadhar_number") : null;
	            String pan = json.optString("pan_number", null);

	            customerDAO.updateCustomerFields(userId, dob, address, aadhar, pan);

	        } 
	        
	        

	        JSONObject response = new JSONObject();
	        response.put("message", "User updated successfully");
	        res.setContentType("application/json");
	        res.getWriter().write(response.toString());

	    } catch (IOException e) {
	        e.printStackTrace();
	        ErrorResponseUtil.send(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
	            new ErrorResponse("TaskException", 500, e.getMessage()));
	    }
    }

    // POST /employee/transactions/deposit
    public void deposit(HttpServletRequest req, HttpServletResponse res) throws TaskException {
    	//Authorization 
	    SessionData sessionData =  new SessionData();
	    sessionData = (SessionData) req.getAttribute("sessionData");
        long employeeId = sessionData.getUserId();
    	
    	 UserDAO userDao = new UserDAO();
            Role authRole = userDao.getUserById(employeeId).getRole();
            if(authRole != Role.EMPLOYEE) {
            	 ErrorResponseUtil.send(res, HttpServletResponse.SC_UNAUTHORIZED,
                         new ErrorResponse("Unauthorized", 403, "Permission Denied"));
            	 return;
            }
    	RequestJsonConverter jsonConverter = new RequestJsonConverter();
	    PojoJsonConverter pojoConverter = new PojoJsonConverter();
	    TransactionDAO transactionDAO = new TransactionDAO();

	    try {
	        JSONObject json = jsonConverter.convertToJson(req);

	        Long accountNumber = json.has("account_number") ? json.getLong("account_number") : null;
	        Double amount = json.has("amount") ? json.getDouble("amount") : null;

	        
	        if (accountNumber == null || amount == null || amount <= 0) {
	            ErrorResponseUtil.send(res, HttpServletResponse.SC_BAD_REQUEST,
	                new ErrorResponse("Bad Request", 400, "account_number and valid amount are required"));
	            return;
	        }

	        //Employee can perform operations on his branch only
 	       // Get employee branch_id
	          EmployeeDAO employeeDAO = new EmployeeDAO();
	          Employee employee = employeeDAO.getEmployeeById(employeeId);
	          long employeeBranchId = employee.getBranchId();

	          // Get account's branch_id
	          AccountDAO accountDAO = new AccountDAO();
	          Account account = accountDAO.getAccountByNumber(accountNumber);
	          long accountBranchId = account.getBranchId();
	          

	          if (employeeBranchId != accountBranchId) {
	              ErrorResponseUtil.send(res, HttpServletResponse.SC_FORBIDDEN,
	                      new ErrorResponse("Unauthorized", 403, "You can only deposit to accounts created in your branch"));
	              return;
	          }
	          
	        // Get session data to track who performed the deposit
	        long doneBy = sessionData.getUserId();

	        TransactionType transactionType = TransactionType.DEPOSIT;
	        
	        TransactionUtil transactionUtil = new TransactionUtil();
            long transactionId = transactionUtil.generateTransactionId();
      
            
       
	        // Perform deposit and return transaction info
	        Transaction transaction = transactionDAO.deposit(accountNumber, amount, doneBy, transactionType, transactionId, null, null);

	        JSONObject jsonResp = pojoConverter.pojoToJson(transaction);
	        jsonResp.put("message", "Deposit successful");

	        res.setContentType("application/json");
	        res.getWriter().write(jsonResp.toString());

	    } catch (IOException  e) {
	        e.printStackTrace();
	        ErrorResponseUtil.send(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
	            new ErrorResponse("TaskException", 500, e.getMessage()));
	       
	    } catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		     ErrorResponseUtil.send(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
			            new ErrorResponse("TaskException", 500, e.getMessage()));
		}
    }

    // POST /employee/transactions/withdraw
    public void withdraw(HttpServletRequest req, HttpServletResponse res) throws TaskException {
    	//Authorization 
	    SessionData sessionData =  new SessionData();
	    sessionData = (SessionData) req.getAttribute("sessionData");
        long employeeId = sessionData.getUserId();
    	
    	 UserDAO userDao = new UserDAO();
            Role authRole = userDao.getUserById(employeeId).getRole();
            if(authRole != Role.EMPLOYEE) {
            	 ErrorResponseUtil.send(res, HttpServletResponse.SC_UNAUTHORIZED,
                         new ErrorResponse("Unauthorized", 403, "Permission Denied"));
            	 return;
            }
            
    	 RequestJsonConverter jsonConverter = new RequestJsonConverter();
 	    PojoJsonConverter pojoConverter = new PojoJsonConverter();
 	    TransactionDAO transactionDAO = new TransactionDAO();

 	    try {
 	        JSONObject json = jsonConverter.convertToJson(req);

 	        Long accountNumber = json.has("account_number") ? json.getLong("account_number") : null;
 	        Double amount = json.has("amount") ? json.getDouble("amount") : null;

 	        if (accountNumber == null || amount == null || amount <= 0) {
 	            ErrorResponseUtil.send(res, HttpServletResponse.SC_BAD_REQUEST,
 	                new ErrorResponse("Bad Request", 400, "account_number and valid amount are required"));
 	            return;
 	        }
 	        
 	        //Employee can perform operations on his branch only
 	       // Get employee branch_id
	          EmployeeDAO employeeDAO = new EmployeeDAO();
	          Employee employee = employeeDAO.getEmployeeById(employeeId);
	          long employeeBranchId = employee.getBranchId();

	          // Get account's branch_id
	          AccountDAO accountDAO = new AccountDAO();
	          Account account = accountDAO.getAccountByNumber(accountNumber);
	          long accountBranchId = account.getBranchId();
	          

	          if (employeeBranchId != accountBranchId) {
	              ErrorResponseUtil.send(res, HttpServletResponse.SC_FORBIDDEN,
	                      new ErrorResponse("Unauthorized", 403, "You can only withdraw from accounts created from your branch"));
	              return;
	          }


 	        // Get session data to track who performed the withdrawal
 	        long doneBy = sessionData.getUserId();

 	        TransactionType transactionType = TransactionType.WITHDRAWAL;
 	        
 	       
 	        // Perform withdrawal and return transaction info
 	        TransactionUtil transactionUtil = new TransactionUtil();
             long transactionId = transactionUtil.generateTransactionId();
			Transaction transaction = transactionDAO.withdraw(accountNumber, amount, doneBy, transactionType, transactionId, null, null);

 	        JSONObject jsonResp = pojoConverter.pojoToJson(transaction);
 	        

 	        res.setContentType("application/json");
 	        res.getWriter().write(jsonResp.toString());

 	    } catch (IOException e) {
 	        e.printStackTrace();
 	        ErrorResponseUtil.send(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
 	            new ErrorResponse("TaskException", 500, e.getMessage()));
 	    } catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		     ErrorResponseUtil.send(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
 			            new ErrorResponse("TaskException", 500, e.getMessage()));
 		}
    }

    // POST /employee/transactions/transfer
    public void transfer(HttpServletRequest req, HttpServletResponse res) throws TaskException {
    	//Authorization 
	    SessionData sessionData =  new SessionData();
	    sessionData = (SessionData) req.getAttribute("sessionData");
        long employeeId = sessionData.getUserId();
    	
    	 UserDAO userDao = new UserDAO();
            Role authRole = userDao.getUserById(employeeId).getRole();
            if(authRole != Role.EMPLOYEE) {
            	 ErrorResponseUtil.send(res, HttpServletResponse.SC_UNAUTHORIZED,
                         new ErrorResponse("Unauthorized", 403, "Permission Denied"));
            	 return;
            }
    	 RequestJsonConverter jsonConverter = new RequestJsonConverter();
 	    TransactionDAO transactionDAO = new TransactionDAO();
 	    TransactionUtil transactionUtil = new TransactionUtil();

 	    try {
 	        JSONObject json = jsonConverter.convertToJson(req);

 	        Long fromAccount = json.has("from_account") ? json.getLong("from_account") : null;
 	        Long toAccount = json.has("to_account") ? json.getLong("to_account") : null;
 	        Double amount = json.has("amount") ? json.getDouble("amount") : null;
 	        String transferType = json.optString("type", null);  // Expected: "INTRA_BANK" or "INTER_BANK"
 	        String ifscCode = json.optString("ifsc_code", null);  // optional

 	        
 	        if (fromAccount == null || toAccount == null || amount == null || amount <= 0 || transferType == null) {
 	            ErrorResponseUtil.send(res, HttpServletResponse.SC_BAD_REQUEST,
 	                new ErrorResponse("Bad Request", 400, "Missing or invalid parameters"));
 	            return;
 	        }

 	       // Get employee branch_id
 	          EmployeeDAO employeeDAO = new EmployeeDAO();
 	          Employee employee = employeeDAO.getEmployeeById(employeeId);
 	          long employeeBranchId = employee.getBranchId();

 	          // Get account's branch_id
 	          AccountDAO accountDAO = new AccountDAO();
 	          Account account = accountDAO.getAccountByNumber(fromAccount);
 	          long accountBranchId = account.getBranchId();
 	          

 	          if (employeeBranchId != accountBranchId) {
 	              ErrorResponseUtil.send(res, HttpServletResponse.SC_FORBIDDEN,
 	                      new ErrorResponse("Unauthorized", 403, "You can only trasfer money from accounts created in your branch"));
 	              return;
 	          }

 	        
 	        
 	        long doneBy = sessionData.getUserId();
 	        long transactionId = transactionUtil.generateTransactionId(); // shared for both rows if intra-bank

 	        if (transferType.equalsIgnoreCase("INTRA_BANK")) {
 	            transactionDAO.withdraw(fromAccount, amount, doneBy, TransactionType.INTRA_BANK_DEBIT, transactionId, toAccount, null);
 	            transactionDAO.deposit(toAccount, amount, doneBy, TransactionType.INTRA_BANK_CREDIT, transactionId, fromAccount, null);
 	        } else if (transferType.equalsIgnoreCase("INTER_BANK")) {
 	            transactionDAO.withdraw(fromAccount, amount, doneBy, TransactionType.INTERBANK_DEBIT, transactionId, toAccount, ifscCode);
 	            // No deposit call for inter-bank – credit is done by external bank
 	        } else {
 	            ErrorResponseUtil.send(res, HttpServletResponse.SC_BAD_REQUEST,
 	                new ErrorResponse("Bad Request", 400, "Invalid transfer type"));
 	            return;
 	        }

 	        // I am the most powerful person onthe entire universe
 	        JSONObject response = new JSONObject();
 	        response.put("message", "Transfer successful");
 	        response.put("transaction_id", transactionId);

 	        res.setContentType("application/json");
 	        res.getWriter().write(response.toString());

 	    } catch (IOException e) {
 	        e.printStackTrace();
 	        ErrorResponseUtil.send(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
 	            new ErrorResponse("TaskException", 500, e.getMessage()));
 	    }
 	    catch (SQLException e) {
 	        e.printStackTrace();
 	        ErrorResponseUtil.send(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
 	            new ErrorResponse("TaskException", 500, e.getMessage()));
 	    }
    }


  // POST /employee/accounts/update
  public void editBranchAccounts(HttpServletRequest req, HttpServletResponse res) throws TaskException {
	//Authorization 
	    SessionData sessionData =  new SessionData();
	    sessionData = (SessionData) req.getAttribute("sessionData");
      long employeeId = sessionData.getUserId();
  	
  	 UserDAO userDao = new UserDAO();
          Role authRole = userDao.getUserById(employeeId).getRole();
          if(authRole != Role.EMPLOYEE) {
          	 ErrorResponseUtil.send(res, HttpServletResponse.SC_UNAUTHORIZED,
                       new ErrorResponse("Unauthorized", 403, "Permission Denied"));
          	 return;
          }
	  RequestJsonConverter jsonConverter = new RequestJsonConverter();
	  AccountDAO accountDAO = new AccountDAO();

      try {
          JSONObject json = jsonConverter.convertToJson(req);

          Long accountNumber = json.has("account_number") ? json.getLong("account_number") : null;
          String operation = json.optString("operation", "").toUpperCase();

          if (accountNumber == null || operation.isEmpty()) {
              ErrorResponseUtil.send(res, HttpServletResponse.SC_BAD_REQUEST,
                      new ErrorResponse("Bad Request", 400, "account_number and operation are required"));
              return;
          }

          
          // Get employee branch_id
          EmployeeDAO employeeDAO = new EmployeeDAO();
          Employee employee = employeeDAO.getEmployeeById(employeeId);
          long employeeBranchId = employee.getBranchId();

          // Get account's branch_id
          Account account = accountDAO.getAccountByNumber(accountNumber);
          long accountBranchId = account.getBranchId();
          

          if (employeeBranchId != accountBranchId) {
              ErrorResponseUtil.send(res, HttpServletResponse.SC_FORBIDDEN,
                      new ErrorResponse("Unauthorized", 403, "You can only update accounts from your branch"));
              return;
          }

          switch (operation) {
              case "ACTIVATE":
            	  accountDAO.changeAccountStatus(accountNumber, "ACTIVE", employeeId);
                  break;
              case "INACTIVATE":
            	  accountDAO.changeAccountStatus(accountNumber, "INACTIVE", employeeId);
                  break;
              case "DELETE":
            	  accountDAO.deleteAccount(accountNumber);
                  break;
              default:
                  ErrorResponseUtil.send(res, HttpServletResponse.SC_BAD_REQUEST,
                          new ErrorResponse("Bad Request", 400, "Invalid operation. Use ACTIVATE, INACTIVATE, or DELETE."));
                  return;
          }

          JSONObject response = new JSONObject();
          response.put("message", "Account " + operation.toLowerCase() + "d successfully");
          res.setContentType("application/json");
          res.getWriter().write(response.toString());

      } catch (IOException e) {
          e.printStackTrace();
          ErrorResponseUtil.send(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                  new ErrorResponse("Server Error", 500, e.getMessage()));
      }
  }
  
  
  
  
  public void updateProfile(HttpServletRequest req, HttpServletResponse res) throws TaskException {
	  
		//Authorization 
	    SessionData sessionData =  new SessionData();
	    sessionData = (SessionData) req.getAttribute("sessionData");
	    long employeeId = sessionData.getUserId();
	
	 UserDAO userDao = new UserDAO();
        Role authRole = userDao.getUserById(employeeId).getRole();
        if(authRole != Role.EMPLOYEE) {
        	 ErrorResponseUtil.send(res, HttpServletResponse.SC_UNAUTHORIZED,
                     new ErrorResponse("Unauthorized", 403, "Permission Denied"));
        	 return;
        }
      RequestJsonConverter jsonConverter = new RequestJsonConverter();
      UserDAO userDAO = new UserDAO();

      try {
          JSONObject json = jsonConverter.convertToJson(req);

          // Get session info
          long userId = sessionData.getUserId();

          // Extract editable fields
          String name = json.optString("name", null);
          String email = json.optString("email", null);
          Long mobileNumber = json.has("mobile_number") ? json.getLong("mobile_number") : null;
          

          // Update users table
          userDAO.updateUserFields(userId, name, email, mobileNumber, userId);


          JSONObject response = new JSONObject();
          response.put("message", "Profile updated successfully");
          res.setContentType("application/json");
          res.getWriter().write(response.toString());

      } catch (IOException e) {
          e.printStackTrace();
          ErrorResponseUtil.send(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
              new ErrorResponse("TaskException", 500, e.getMessage()));
      }
  }
  
  
  // Get account data by customer id or account number
  public void getAccountDetails(HttpServletRequest req, HttpServletResponse res) throws TaskException {
	    AccountDAO accountDAO = new AccountDAO();
	    PojoJsonConverter converter = new PojoJsonConverter();

	    try {
	        // Authorization
	        SessionData sessionData = (SessionData) req.getAttribute("sessionData");
	        long employeeId = sessionData.getUserId();

	        UserDAO userDao = new UserDAO();
	        Role authRole = userDao.getUserById(employeeId).getRole();
	        if (authRole != Role.EMPLOYEE) {
	            ErrorResponseUtil.send(res, HttpServletResponse.SC_UNAUTHORIZED,
	                    new ErrorResponse("Unauthorized", 403, "Permission Denied"));
	            return;
	        }

	        String accountNumParam = req.getParameter("account_number");
	        String customerIdParam = req.getParameter("customer_id");

	        // Debug print
	        System.out.println("account_number param: " + accountNumParam);
	        System.out.println("customer_id param: " + customerIdParam);

	        // Get employee branch_id
	        EmployeeDAO employeeDAO = new EmployeeDAO();
	        Employee employee = employeeDAO.getEmployeeById(employeeId);
	        long employeeBranchId = employee.getBranchId();

	        List<Account> accounts = new ArrayList<>();

	        if (accountNumParam != null && !accountNumParam.isEmpty()) {
	            long accountNumber = Long.parseLong(accountNumParam.trim());

	            // Get account's branch_id
	            Account account = accountDAO.getAccountByNumber(accountNumber);
	            if (account == null) {
	                ErrorResponseUtil.send(res, HttpServletResponse.SC_NOT_FOUND,
	                        new ErrorResponse("Not Found", 404, "Account not found"));
	                return;
	            }

	            long accountBranchId = account.getBranchId();
	            if (employeeBranchId != accountBranchId) {
	                ErrorResponseUtil.send(res, HttpServletResponse.SC_FORBIDDEN,
	                        new ErrorResponse("Unauthorized", 403, "You can only access accounts from your branch"));
	                return;
	            }

	            accounts.add(account);
	        } else if (customerIdParam != null && !customerIdParam.isEmpty()) {
	            long customerId = Long.parseLong(customerIdParam.trim());

	            // Get accounts by customer and branch
	            accounts = accountDAO.getAccountsByCustomerAndBranch(customerId, employeeBranchId);
	        } else {
	            ErrorResponseUtil.send(res, HttpServletResponse.SC_BAD_REQUEST,
	                    new ErrorResponse("Bad Request", 400, "Provide either account_number or customer_id"));
	            return;
	        }

	        // Convert to JSON
	        JSONArray jsonArray = new JSONArray();
	        for (Account acc : accounts) {
	            jsonArray.put(converter.pojoToJson(acc));
	        }

	        JSONObject response = new JSONObject();
	        response.put("accounts", jsonArray);

	        res.setContentType("application/json");
	        res.getWriter().write(response.toString());

	    } catch (NumberFormatException e) {
	        ErrorResponseUtil.send(res, HttpServletResponse.SC_BAD_REQUEST,
	                new ErrorResponse("Invalid number format", 400, "account_number or customer_id must be numeric"));
	    } catch (IOException e) {
	        ErrorResponseUtil.send(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
	                new ErrorResponse("Error", 500, e.getMessage()));
	    }
	}


 
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
//// GET /employee/branches/requests
//public void getBranchRequests(HttpServletRequest req, HttpServletResponse res) throws IOException {
//    res.getWriter().write("{\"status\":\"EmployeeHandler.getBranchRequests not implemented\"}");
//}

  
  
  
}
