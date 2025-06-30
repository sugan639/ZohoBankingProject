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
import com.sbank.netbanking.dao.BranchDAO;
import com.sbank.netbanking.dao.CustomerDAO;
import com.sbank.netbanking.dao.EmployeeDAO;
import com.sbank.netbanking.dao.TransactionDAO;
import com.sbank.netbanking.dao.UserDAO;
import com.sbank.netbanking.dto.ErrorResponse;
import com.sbank.netbanking.dto.UserDTO;
import com.sbank.netbanking.exceptions.TaskException;
import com.sbank.netbanking.model.Account;
import com.sbank.netbanking.model.Branch;
import com.sbank.netbanking.model.Customer;
import com.sbank.netbanking.model.Employee;
import com.sbank.netbanking.model.NewCustomer;
import com.sbank.netbanking.model.SessionData;
import com.sbank.netbanking.model.Transaction;
import com.sbank.netbanking.model.Transaction.TransactionType;
import com.sbank.netbanking.model.User;
import com.sbank.netbanking.model.User.Role;
import com.sbank.netbanking.service.BcryptService;
import com.sbank.netbanking.util.BranchUtil;
import com.sbank.netbanking.util.DateUtil;
import com.sbank.netbanking.util.ErrorResponseUtil;
import com.sbank.netbanking.util.PojoJsonConverter;
import com.sbank.netbanking.util.RandomPasswordGenerator;
import com.sbank.netbanking.util.RequestJsonConverter;
import com.sbank.netbanking.util.TransactionUtil;
import com.sbank.netbanking.util.UserMapper;

public class AdminHandler {

	// 1. Get admin profile
	public void getProfile(HttpServletRequest req, HttpServletResponse res) throws TaskException {

		
		
	    SessionData sessionData =  new SessionData();
	    sessionData = (SessionData) req.getAttribute("sessionData");
        long adminId = sessionData.getUserId();
        
      
      
        System.out.println("User ID from the session data get profile method: "+adminId);
        EmployeeDAO employeeDAO = new EmployeeDAO();
        PojoJsonConverter converter = new PojoJsonConverter();

        try {
        	
        	  //Authorising only admin
    	    UserDAO userDao = new UserDAO();
            Role role = userDao.getUserById(adminId).getRole();
            if(role != Role.ADMIN) {
            	 ErrorResponseUtil.send(res, HttpServletResponse.SC_UNAUTHORIZED,
                         new ErrorResponse("Unauthorized", 403, "Permission Denied"));
            	 return;
            }
            
        	Employee admin = employeeDAO.getEmployeeById(adminId);
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
    

	
	
	// 2. GET /admin/branches/{branch_id}
	public void getBranchById(HttpServletRequest req, HttpServletResponse res) throws TaskException {
	    try {

	    	//Authorization 
		    SessionData sessionData =  new SessionData();
		    sessionData = (SessionData) req.getAttribute("sessionData");
	        long adminId = sessionData.getUserId();
	    	
	    	 UserDAO userDao = new UserDAO();
	            Role role = userDao.getUserById(adminId).getRole();
	            if(role != Role.ADMIN) {
	            	 ErrorResponseUtil.send(res, HttpServletResponse.SC_UNAUTHORIZED,
	                         new ErrorResponse("Unauthorized", 403, "Permission Denied"));
	            	 return;
	            }
	            
	            
	        String branchIdStr = req.getParameter("branchId");

	        if (branchIdStr == null || branchIdStr.isEmpty()) {
	            throw new TaskException("Missing or invalid 'branchId' in query parameters.");
	        }

	        long branchId;
	        try {
	            branchId = Long.parseLong(branchIdStr);
	        } catch (NumberFormatException e) {
	            throw new TaskException("Invalid branch ID format. Must be a number.", e);
	        }

	       


	        Branch branch = new Branch();
	        BranchDAO branchDao = new BranchDAO();

	        
	        branch = branchDao.getBranchById(branchId);  // Branch POJO
	        System.out.println("Branch ID from the GET request data @ getBranch method: "+branchId);

	        PojoJsonConverter converter = new PojoJsonConverter();
	        JSONObject jsonBranch = converter.pojoToJson(branch); // Branch JSON

	       
	        res.setContentType("application/json");
	        res.getWriter().write(jsonBranch.toString());

	    } catch (NumberFormatException e) {
	        throw new TaskException("Invalid branch ID format", e);
	    } catch (IOException e) {
	        throw new TaskException("Failed to write branch response", e);
	    }
	}

	// 3. PATCH /admin/branches     // Working
	public void updateBranch(HttpServletRequest req, HttpServletResponse res) throws TaskException {
	    try {
	        RequestJsonConverter requestJsonConverter = new RequestJsonConverter();
	        JSONObject json = requestJsonConverter.convertToJson(req);

			
		    SessionData sessionData =  new SessionData();
		    sessionData = (SessionData) req.getAttribute("sessionData");
	        long adminId = sessionData.getUserId();		// UserID from session data
	      
	      	//Authorization 
		 	 UserDAO userDao = new UserDAO();
	            Role role = userDao.getUserById(adminId).getRole();
	            if(role != Role.ADMIN) {
	            	 ErrorResponseUtil.send(res, HttpServletResponse.SC_UNAUTHORIZED,
	                         new ErrorResponse("Unauthorized", 403, "Permission Denied"));
	            	 return;
	            }
	            
	        Long branchId = json.has("branch_id") ? json.getLong("branch_id") : null;
	      
	        String ifscCode = json.optString("ifsc_code", null);
	        String bankName = json.optString("bank_name", null);
	        String location = json.optString("location", null);

	        if (branchId == null ) {
	            ErrorResponse error = new ErrorResponse("Bad request", 400, "branch_id is required");
	            ErrorResponseUtil.send(res, HttpServletResponse.SC_BAD_REQUEST, error);
	            return;
	        }

	        BranchDAO dao = new BranchDAO();
	        Branch updatedBranch = dao.updateBranch(branchId, adminId, ifscCode, bankName, location);

	        if (updatedBranch != null) {
	            PojoJsonConverter converter = new PojoJsonConverter();
	            JSONObject responseJson = converter.pojoToJson(updatedBranch);
	            res.setContentType("application/json");
	            res.getWriter().write(responseJson.toString());
	        } else {
	            ErrorResponse error = new ErrorResponse("Update Failed", 404, "Branch not found or update unsuccessful");
	            ErrorResponseUtil.send(res, HttpServletResponse.SC_NOT_FOUND, error);
	        }

	    } catch ( IOException e) {
	        ErrorResponse error = new ErrorResponse("Server Error", 500, e.getMessage());
	        ErrorResponseUtil.send(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error);
	    }
	}

	
	// 4. GET /admin/users/{user_id}
	public void getUser(HttpServletRequest req, HttpServletResponse res) throws  TaskException {
		
	  	//Authorization 
	    SessionData sessionData =  new SessionData();
	    sessionData = (SessionData) req.getAttribute("sessionData");
        long adminId = sessionData.getUserId();
    	
    	 UserDAO userDao = new UserDAO();
            Role role = userDao.getUserById(adminId).getRole();
            if(role != Role.ADMIN) {
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



	// 5. PUT /admin/users/update
	public void updateUser(HttpServletRequest req, HttpServletResponse res) throws TaskException {
		
		
	    RequestJsonConverter jsonConverter = new RequestJsonConverter();
	    UserDAO userDAO = new UserDAO();
        EmployeeDAO employeeDAO = new EmployeeDAO();
	    CustomerDAO customerDAO = new CustomerDAO();
	    
	    

	    try {
	    	
	      	//Authorization 
		    SessionData sessionData =  new SessionData();
		    sessionData = (SessionData) req.getAttribute("sessionData");
	        long adminId = sessionData.getUserId();
	    	
	    	 UserDAO userDao = new UserDAO();
	            Role authRole = userDao.getUserById(adminId).getRole();
	            if(authRole != Role.ADMIN) {
	            	 ErrorResponseUtil.send(res, HttpServletResponse.SC_UNAUTHORIZED,
	                         new ErrorResponse("Unauthorized", 403, "Permission Denied"));
	            	 return;
	            }
	            
	    	
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

	        } else if (role.equals("EMPLOYEE") || role.equals("ADMIN")) {
	            // Validate that no customer-specific fields are present
	            if (json.has("dob") || json.has("address") || json.has("aadhar_number") || json.has("pan_number")) {
	                ErrorResponseUtil.send(res, HttpServletResponse.SC_BAD_REQUEST,
	                    new ErrorResponse("Bad Request", 400, "Employees/Admins should not have customer-specific fields like dob, address, etc."));
	                return;
	            }

	            Long branchId = json.has("branch_id") ? json.getLong("branch_id") : null;
	            employeeDAO.updateEmployeeFields(userId, branchId);
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


	
	
	// 6. POST /admin/transactions/deposit
	public void deposit(HttpServletRequest req, HttpServletResponse res) throws IOException, TaskException {
	    RequestJsonConverter jsonConverter = new RequestJsonConverter();
	    PojoJsonConverter pojoConverter = new PojoJsonConverter();
	    TransactionDAO transactionDAO = new TransactionDAO();

	    try {
	    	
	    	//Authorization 
		    SessionData sessionData =  new SessionData();
		    sessionData = (SessionData) req.getAttribute("sessionData");
	        long adminId = sessionData.getUserId();
	    	
	    	 UserDAO userDao = new UserDAO();
	            Role authRole = userDao.getUserById(adminId).getRole();
	            if(authRole != Role.ADMIN) {
	            	 ErrorResponseUtil.send(res, HttpServletResponse.SC_UNAUTHORIZED,
	                         new ErrorResponse("Unauthorized", 403, "Permission Denied"));
	            	 return;
	            }
	            
	        JSONObject json = jsonConverter.convertToJson(req);

	        Long accountNumber = json.has("account_number") ? json.getLong("account_number") : null;
	        Double amount = json.has("amount") ? json.getDouble("amount") : null;

	        if (accountNumber == null || amount == null || amount <= 0) {
	            ErrorResponseUtil.send(res, HttpServletResponse.SC_BAD_REQUEST,
	                new ErrorResponse("Bad Request", 400, "account_number and valid amount are required"));
	            return;
	        }

	        // Get session data to track who performed the deposit
	        long doneBy = sessionData.getUserId();

	        TransactionType transactionType = TransactionType.DEPOSIT;
	        
	        TransactionUtil transactionUtil = new TransactionUtil();
            long transactionId = transactionUtil.generateTransactionId();
            @SuppressWarnings("null")
            long fromAccount = (Long) null;
	        
	        // Perform deposit and return transaction info
	        Transaction transaction = transactionDAO.deposit(accountNumber, amount, doneBy, transactionType, transactionId, fromAccount, null);

	        JSONObject jsonResp = pojoConverter.pojoToJson(transaction);
	        jsonResp.put("message", "Deposit successful");

	        res.setContentType("application/json");
	        res.getWriter().write(jsonResp.toString());

	    } catch (TaskException  e) {
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
	
	
	

	
	// 7. POST /admin/transactions/withdraw
	public void withdraw(HttpServletRequest req, HttpServletResponse res) throws TaskException {
	    RequestJsonConverter jsonConverter = new RequestJsonConverter();
	    PojoJsonConverter pojoConverter = new PojoJsonConverter();
	    TransactionDAO transactionDAO = new TransactionDAO();

	    try {
	    	
	    	//Authorization 
		    SessionData sessionData =  new SessionData();
		    sessionData = (SessionData) req.getAttribute("sessionData");
	        long adminId = sessionData.getUserId();
	    	
	    	 UserDAO userDao = new UserDAO();
	            Role authRole = userDao.getUserById(adminId).getRole();
	            if(authRole != Role.ADMIN) {
	            	 ErrorResponseUtil.send(res, HttpServletResponse.SC_UNAUTHORIZED,
	                         new ErrorResponse("Unauthorized", 403, "Permission Denied"));
	            	 return;
	            }
	            
	        JSONObject json = jsonConverter.convertToJson(req);

	        Long accountNumber = json.has("account_number") ? json.getLong("account_number") : null;
	        Double amount = json.has("amount") ? json.getDouble("amount") : null;

	        if (accountNumber == null || amount == null || amount <= 0) {
	            ErrorResponseUtil.send(res, HttpServletResponse.SC_BAD_REQUEST,
	                new ErrorResponse("Bad Request", 400, "account_number and valid amount are required"));
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

	
	
	// 8. POST /admin/transactions/transfer
	public void transfer(HttpServletRequest req, HttpServletResponse res) throws TaskException {
	    RequestJsonConverter jsonConverter = new RequestJsonConverter();
	    TransactionDAO transactionDAO = new TransactionDAO();
	    TransactionUtil transactionUtil = new TransactionUtil();

	    try {
	    	//Authorization 
		    SessionData sessionData =  new SessionData();
		    sessionData = (SessionData) req.getAttribute("sessionData");
	        long adminId = sessionData.getUserId();
	    	
	    	 UserDAO userDao = new UserDAO();
	            Role authRole = userDao.getUserById(adminId).getRole();
	            if(authRole != Role.ADMIN) {
	            	 ErrorResponseUtil.send(res, HttpServletResponse.SC_UNAUTHORIZED,
	                         new ErrorResponse("Unauthorized", 403, "Permission Denied"));
	            	 return;
	            }
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


    
    
    
    
    
    
    
    
	// POST /admin/transactions/query
	public void queryTransactions(HttpServletRequest req, HttpServletResponse res) throws TaskException {
	    RequestJsonConverter converter = new RequestJsonConverter();
	    PojoJsonConverter pojoConverter = new PojoJsonConverter();
	    TransactionDAO transactionDAO = new TransactionDAO();

	    try {
	    	
	    	//Authorization 
		    SessionData sessionData =  new SessionData();
		    sessionData = (SessionData) req.getAttribute("sessionData");
	        long adminId = sessionData.getUserId();
	    	
	    	 UserDAO userDao = new UserDAO();
	            Role authRole = userDao.getUserById(adminId).getRole();
	            if(authRole != Role.ADMIN) {
	            	 ErrorResponseUtil.send(res, HttpServletResponse.SC_UNAUTHORIZED,
	                         new ErrorResponse("Unauthorized", 403, "Permission Denied"));
	            	 return;
	            }
	            
	        // Getting request body parameters
	        JSONObject json = converter.convertToJson(req);
	        Long customerId = json.has("customer_id") ? json.getLong("customer_id") : null;
	        Long txnId = json.has("transaction_id") ? json.getLong("transaction_id") : null;
	        Long refNum = json.has("transaction_reference_number") ? json.getLong("transaction_reference_number") : null;
	        Long accNum = json.has("account_number") ? json.getLong("account_number") : null;
	        Long from = json.has("from_date") ? json.getLong("from_date") : null;
	        Long to = json.has("to_date") ? json.getLong("to_date") : null;
	        String type = json.optString("type", null);
	        String status = json.optString("status", null);
	        int limit = json.has("limit") ? json.getInt("limit") : 100;
	        int offset = json.has("offset") ? json.getInt("offset") : 0;

	        // Validation
	        boolean hasTxnId = txnId != null;
	        boolean hasRefNum = refNum != null;
	        boolean hasAccountFilter = accNum != null && from != null && to != null;
	        boolean hasCustomerFilter = customerId != null && from != null && to != null;

	        if (!(hasTxnId || hasRefNum || hasAccountFilter || hasCustomerFilter)) {
	            ErrorResponseUtil.send(res, HttpServletResponse.SC_BAD_REQUEST,
	                new ErrorResponse("Bad Request", 400, "Provide transaction_id OR reference_number OR (account_number or customer_id) with from_date and to_date"));
	            return;
	        }


	        List<Transaction> txns = transactionDAO.getFilteredTransactions(
	            txnId, refNum, accNum,customerId, from, to, type, status, limit, offset
	        );

	        JSONArray jsonArr = pojoConverter.pojoListToJsonArray(txns);
	        JSONObject result = new JSONObject();
	        result.put("transactions", jsonArr);

	        res.setContentType("application/json");
	        res.getWriter().write(result.toString());

	    } catch (IOException e) {
	        e.printStackTrace();
	        ErrorResponseUtil.send(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
	            new ErrorResponse("Server Error", 500, "Could not fetch transactions"));
	    }
	}

    
    
    
   
    
    
    
    
    
    
 // POST /admin/customer/account
    public void createAccount(HttpServletRequest req, HttpServletResponse res) throws TaskException {
        RequestJsonConverter jsonConverter = new RequestJsonConverter();
        PojoJsonConverter pojoConverter = new PojoJsonConverter();
        AccountDAO accountDAO = new AccountDAO();
        UserDAO userDAO = new UserDAO();

        try {
        	//Authorization 
		    SessionData sessionData =  new SessionData();
		    sessionData = (SessionData) req.getAttribute("sessionData");
	        long adminId = sessionData.getUserId();
	    	
	    	 UserDAO userDao = new UserDAO();
	            Role authRole = userDao.getUserById(adminId).getRole();
	            if(authRole != Role.ADMIN) {
	            	 ErrorResponseUtil.send(res, HttpServletResponse.SC_UNAUTHORIZED,
	                         new ErrorResponse("Unauthorized", 403, "Permission Denied"));
	            	 return;
	            }
            JSONObject json = jsonConverter.convertToJson(req);

            Long userId = json.has("user_id") ? json.getLong("user_id") : null;
            Long branchId = json.has("branch_id") ? json.getLong("branch_id") : null;
            Double initialBalance = json.has("balance") ? json.getDouble("balance") : 0.0;

            if (userId == null || branchId == null) {
                ErrorResponseUtil.send(res, HttpServletResponse.SC_BAD_REQUEST,
                    new ErrorResponse("Bad Request", 400, "user_id and branch_id are required"));
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

    
    
    
    
    // 10. POST /admin/enew-mployee
    public void addEmployee(HttpServletRequest req, HttpServletResponse res) throws TaskException {
        RequestJsonConverter jsonConverter = new RequestJsonConverter();
        PojoJsonConverter pojoConverter = new PojoJsonConverter();
        EmployeeDAO employeeDAO = new EmployeeDAO();
        RandomPasswordGenerator randomPassword = new RandomPasswordGenerator();

        try {
        	
        	//Authorization 
		    SessionData sessionData =  new SessionData();
		    sessionData = (SessionData) req.getAttribute("sessionData");
	        long adminId = sessionData.getUserId();
	    	
	    	 UserDAO userDao = new UserDAO();
	            Role authRole = userDao.getUserById(adminId).getRole();
	            if(authRole != Role.ADMIN) {
	            	 ErrorResponseUtil.send(res, HttpServletResponse.SC_UNAUTHORIZED,
	                         new ErrorResponse("Unauthorized", 403, "Permission Denied"));
	            	 return;
	            }
	            
            // Get JSON from request
            JSONObject json = jsonConverter.convertToJson(req);

            // Required fields
            String name = json.optString("name", null);
            String email = json.optString("email", null);
            Long mobileNumber = json.has("mobile_number") ? json.getLong("mobile_number") : null;
            Long branchId = json.has("branch_id") ? json.getLong("branch_id") : null;
            Role role = Role.valueOf(json.optString("role", "EMPLOYEE").toUpperCase());
            
            // Generate secure password
            String password = randomPassword.generateRandomPassword(8);
            String hashedPassword = BcryptService.hashPassword(password);

            // Get admin ID from session for tracking
            long createdBy = sessionData.getUserId();

            // Validate role
            if (!role.equals(Role.ADMIN) && !role.equals(Role.EMPLOYEE)) {
                ErrorResponseUtil.send(res, HttpServletResponse.SC_BAD_REQUEST,
                    new ErrorResponse("Bad Request", 400, "Role must be either 'ADMIN' or 'EMPLOYEE'"));
                return;
            }

            // Validate required fields
            if (name == null || email == null || mobileNumber == null || branchId == null) {
                ErrorResponseUtil.send(res, HttpServletResponse.SC_BAD_REQUEST,
                        new ErrorResponse("Bad Request", 400, "All fields are required"));
                return;
            }
            
            

            // Save to DB
            Employee employee = employeeDAO.addNewEmployee(name, hashedPassword, email, mobileNumber, branchId, role, createdBy);

            // Convert to JSON and return
            JSONObject responseJson = pojoConverter.pojoToJson(employee);
            responseJson.put("message", "User added successfully");
            responseJson.put("initial_password", password); //  Include password ONLY HERE

            res.setContentType("application/json");
            res.getWriter().write(responseJson.toString());

        } catch (IOException e) {
        	 e.printStackTrace();
            ErrorResponseUtil.send(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    new ErrorResponse("TaskException", 500, e.getMessage()));
            
        }
    }
    
    
    
    
 // POST /admin/customer
    public void addCustomer(HttpServletRequest req, HttpServletResponse res) throws TaskException {
        RequestJsonConverter jsonConverter = new RequestJsonConverter();
        PojoJsonConverter pojoConverter = new PojoJsonConverter();
        CustomerDAO customerDAO = new CustomerDAO();
        RandomPasswordGenerator passwordGenerator = new RandomPasswordGenerator();

        try {
        	//Authorization 
		    SessionData sessionData =  new SessionData();
		    sessionData = (SessionData) req.getAttribute("sessionData");
	        long adminId = sessionData.getUserId();
	    	
	    	 UserDAO userDao = new UserDAO();
	            Role authRole = userDao.getUserById(adminId).getRole();
	            if(authRole != Role.ADMIN) {
	            	 ErrorResponseUtil.send(res, HttpServletResponse.SC_UNAUTHORIZED,
	                         new ErrorResponse("Unauthorized", 403, "Permission Denied"));
	            	 return;
	            }
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

    
 // 11. PUT /admin/account/status
    public void updateAccountStatus(HttpServletRequest req, HttpServletResponse res) throws TaskException {
        RequestJsonConverter jsonConverter = new RequestJsonConverter();
        AccountDAO accountDAO = new AccountDAO();

        try {
        	//Authorization 
		    SessionData sessionData =  new SessionData();
		    sessionData = (SessionData) req.getAttribute("sessionData");
	        long adminId = sessionData.getUserId();
	    	
	    	 UserDAO userDao = new UserDAO();
	            Role authRole = userDao.getUserById(adminId).getRole();
	            if(authRole != Role.ADMIN) {
	            	 ErrorResponseUtil.send(res, HttpServletResponse.SC_UNAUTHORIZED,
	                         new ErrorResponse("Unauthorized", 403, "Permission Denied"));
	            	 return;
	            }
            JSONObject json = jsonConverter.convertToJson(req);

            Long accountNumber = json.has("account_number") ? json.getLong("account_number") : null;
            String operation = json.optString("operation", "").toUpperCase();

            if (accountNumber == null || operation.isEmpty()) {
                ErrorResponseUtil.send(res, HttpServletResponse.SC_BAD_REQUEST,
                        new ErrorResponse("Bad Request", 400, "account_number and operation are required"));
                return;
            }

            // Get modifier (admin) from session
            long modifiedBy = sessionData.getUserId();

            switch (operation) {
                case "ACTIVATE":
                	accountDAO.changeAccountStatus(accountNumber, "ACTIVE", modifiedBy);
                    break;
                case "INACTIVATE":
                	accountDAO.changeAccountStatus(accountNumber, "INACTIVE", modifiedBy);
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

    
 // POST /admin/branches
    public void createBranch(HttpServletRequest req, HttpServletResponse res) throws TaskException {
        RequestJsonConverter jsonConverter = new RequestJsonConverter();
        PojoJsonConverter pojoConverter = new PojoJsonConverter();
        BranchDAO branchDAO = new BranchDAO();

        try {
        	//Authorization 
		    SessionData sessionData =  new SessionData();
		    sessionData = (SessionData) req.getAttribute("sessionData");
	        long adminId = sessionData.getUserId();
	    	
	    	 UserDAO userDao = new UserDAO();
	            Role authRole = userDao.getUserById(adminId).getRole();
	            if(authRole != Role.ADMIN) {
	            	 ErrorResponseUtil.send(res, HttpServletResponse.SC_UNAUTHORIZED,
	                         new ErrorResponse("Unauthorized", 403, "Permission Denied"));
	            	 return;
	            }
            JSONObject json = jsonConverter.convertToJson(req);

            String newAdminIdString = json.optString("new_admin_id");
            String ifscCode = json.optString("ifsc_code");
            String bankName = json.optString("bank_name");
            String location = json.optString("location");

            if (newAdminIdString == null || bankName == null || bankName.isEmpty() || location == null || location.isEmpty()) {
                ErrorResponseUtil.send(res, HttpServletResponse.SC_BAD_REQUEST,
                        new ErrorResponse("Bad Request", 400, "bank_name and location are required"));
                return;
            }


            // Generate IFSC code if not provided
            if (ifscCode == null || ifscCode.isEmpty()) {
            	
            	BranchUtil branchUtil = new BranchUtil(); 
                ifscCode = branchUtil.generateIfscCode(bankName, location);
            }

        	Long newAdminId = Long.parseLong(newAdminIdString);

            Branch branch = new Branch();
            branch.setAdminId(newAdminId);
            branch.setIfscCode(ifscCode);
            branch.setBankName(bankName);
            branch.setLocation(location);

            // Create the branch
            Branch createdBranch = branchDAO.createBranch(branch, adminId);

            JSONObject responseJson = pojoConverter.pojoToJson(createdBranch);
            responseJson.put("message", "Branch created successfully");

            res.setContentType("application/json");
            res.getWriter().write(responseJson.toString());

        } catch (IOException e) {
            ErrorResponseUtil.send(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    new ErrorResponse("Error", 500, e.getMessage()));
        }
    }
    
 // GET /admin/accounts
    public void getAccountDetails(HttpServletRequest req, HttpServletResponse res) throws TaskException {
        AccountDAO accountDAO = new AccountDAO();
        PojoJsonConverter converter = new PojoJsonConverter();

        try {
        	//Authorization 
		    SessionData sessionData =  new SessionData();
		    sessionData = (SessionData) req.getAttribute("sessionData");
	        long adminId = sessionData.getUserId();
	    	
	    	 UserDAO userDao = new UserDAO();
	            Role authRole = userDao.getUserById(adminId).getRole();
	            if(authRole != Role.ADMIN) {
	            	 ErrorResponseUtil.send(res, HttpServletResponse.SC_UNAUTHORIZED,
	                         new ErrorResponse("Unauthorized", 403, "Permission Denied"));
	            	 return;
	            }
            String accountNumParam = req.getParameter("account_number");
            String customerIdParam = req.getParameter("customer_id");

            List<Account> accounts = new ArrayList<>();

            if (accountNumParam != null) {
                // Fetch by account_number
                long accountNumber = Long.parseLong(accountNumParam);
                Account acc = accountDAO.getAccountByNumber(accountNumber);
                if (acc != null) {
                    accounts.add(acc);
                }
            } else if (customerIdParam != null) {
                // Fetch all accounts by customer_id
                long customerId = Long.parseLong(customerIdParam);
                accounts = accountDAO.getAccountsByUserId(customerId);
            } else {
                ErrorResponseUtil.send(res, HttpServletResponse.SC_BAD_REQUEST,
                        new ErrorResponse("Bad Request", 400, "Provide either account_number or customer_id"));
                return;
            }

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
                    new ErrorResponse("Bad Request", 400, "Invalid number format"));
        } catch (IOException e) {
            ErrorResponseUtil.send(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    new ErrorResponse("Error", 500, e.getMessage()));
        }
    }


   

}