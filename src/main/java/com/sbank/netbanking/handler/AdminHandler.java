package com.sbank.netbanking.handler;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.sbank.netbanking.dao.AdminDAO;
import com.sbank.netbanking.dao.BranchDAO;
import com.sbank.netbanking.dao.CustomerDAO;
import com.sbank.netbanking.dao.TransactionDAO;
import com.sbank.netbanking.dao.TransactionUtil;
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
import com.sbank.netbanking.util.DateUtil;
import com.sbank.netbanking.util.ErrorResponseUtil;
import com.sbank.netbanking.util.PojoJsonConverter;
import com.sbank.netbanking.util.RandomPasswordGenerator;
import com.sbank.netbanking.util.RequestJsonConverter;
import com.sbank.netbanking.util.UserMapper;

public class AdminHandler {

	// 1. Get admin profile
	public void getProfile(HttpServletRequest req, HttpServletResponse res) throws TaskException {

		
		
	    SessionData sessionData =  new SessionData();
	    sessionData = (SessionData) req.getAttribute("sessionData");
        long adminId = sessionData.getUserId();
      
        System.out.println("User ID from the session data get profile method: "+adminId);
        AdminDAO adminDAO = new AdminDAO();
        PojoJsonConverter converter = new PojoJsonConverter();

        try {
        	Employee admin = adminDAO.getEmployeeById(adminId);
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
	            AdminDAO adminDAO = new AdminDAO();
	            Employee employee = adminDAO.getEmployeeById(userId);
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
	    AdminDAO adminDAO = new AdminDAO();
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

	        SessionData sessionData = (SessionData) req.getAttribute("sessionData");
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
	            adminDAO.updateEmployeeFields(userId, branchId);
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
	        JSONObject json = jsonConverter.convertToJson(req);

	        Long accountNumber = json.has("account_number") ? json.getLong("account_number") : null;
	        Double amount = json.has("amount") ? json.getDouble("amount") : null;

	        if (accountNumber == null || amount == null || amount <= 0) {
	            ErrorResponseUtil.send(res, HttpServletResponse.SC_BAD_REQUEST,
	                new ErrorResponse("Bad Request", 400, "account_number and valid amount are required"));
	            return;
	        }

	        // Get session data to track who performed the deposit
	        SessionData sessionData = (SessionData) req.getAttribute("sessionData");
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
	        JSONObject json = jsonConverter.convertToJson(req);

	        Long accountNumber = json.has("account_number") ? json.getLong("account_number") : null;
	        Double amount = json.has("amount") ? json.getDouble("amount") : null;

	        if (accountNumber == null || amount == null || amount <= 0) {
	            ErrorResponseUtil.send(res, HttpServletResponse.SC_BAD_REQUEST,
	                new ErrorResponse("Bad Request", 400, "account_number and valid amount are required"));
	            return;
	        }

	        // Get session data to track who performed the withdrawal
	        SessionData sessionData = (SessionData) req.getAttribute("sessionData");
	        long doneBy = sessionData.getUserId();

	        TransactionType transactionType = TransactionType.WITHDRAWAL;
	        
	        @SuppressWarnings("null")
			long toAccount = (Long) null;
	       
	        // Perform withdrawal and return transaction info
	        TransactionUtil transactionUtil = new TransactionUtil();
            long transactionId = transactionUtil.generateTransactionId();
	        Transaction transaction = transactionDAO.withdraw(accountNumber, amount, doneBy, transactionType, transactionId, toAccount, null);

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
	        JSONObject json = jsonConverter.convertToJson(req);

	        Long fromAccount = json.has("from_account") ? json.getLong("from_account") : null;
	        Long toAccount = json.has("to_account") ? json.getLong("to_account") : null;
	        Double amount = json.has("amount") ? json.getDouble("amount") : null;
	        String transferType = json.optString("type", null);  // Expected: "INTRA_BANK" or "INTER_BANK"
	        String ifscCode = json.optString("ifsc_code", null);  // optional
	        SessionData sessionData = (SessionData) req.getAttribute("sessionData");

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


    
    
    
    
    
    
    
    
    
    
    
    
   
    
    
    
    
    
    
    
    
    
    
    
    
    
 // POST /admin/customer/account
    public void createAccount(HttpServletRequest req, HttpServletResponse res) throws TaskException {
        RequestJsonConverter jsonConverter = new RequestJsonConverter();
        PojoJsonConverter pojoConverter = new PojoJsonConverter();
        AdminDAO adminDAO = new AdminDAO();
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

            // Validate user is a customer
            User user = userDAO.getUserById(userId);
            if (user == null || !user.getRole().name().equals("CUSTOMER")) {
                ErrorResponseUtil.send(res, HttpServletResponse.SC_BAD_REQUEST,
                    new ErrorResponse("Bad Request", 400, "Account can only be created for customers"));
                return;
            }

            // Get admin ID from session (created_by, modified_by)
            SessionData sessionData = (SessionData) req.getAttribute("sessionData");
            long createdBy = sessionData.getUserId();

            // Create the account
            Account account = adminDAO.createCustomerAccount(userId, branchId, initialBalance, createdBy);

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
        AdminDAO adminDAO = new AdminDAO();
        RandomPasswordGenerator randomPassword = new RandomPasswordGenerator();

        try {
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

            // Get admin ID from session for tracking
            SessionData sessionData = (SessionData) req.getAttribute("sessionData");
            long createdBy = sessionData.getUserId();

            // Validate role
            if (!role.equals("ADMIN") && !role.equals("EMPLOYEE")) {
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
            Employee employee = adminDAO.addNewEmployee(name, password, email, mobileNumber, branchId, role, createdBy);

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
        AdminDAO adminDAO = new AdminDAO();
        RandomPasswordGenerator passwordGenerator = new RandomPasswordGenerator();

        try {
            // Extract JSON from request
            JSONObject json = jsonConverter.convertToJson(req);

            // Get session data (to know who created the customer)
            SessionData sessionData = (SessionData) req.getAttribute("sessionData");
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
            NewCustomer customer = adminDAO.addNewCustomer(name, hashedPassword, email, mobileNumber, dobMillis,
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
        AdminDAO adminDAO = new AdminDAO();

        try {
            JSONObject json = jsonConverter.convertToJson(req);

            Long accountNumber = json.has("account_number") ? json.getLong("account_number") : null;
            String operation = json.optString("operation", "").toUpperCase();

            if (accountNumber == null || operation.isEmpty()) {
                ErrorResponseUtil.send(res, HttpServletResponse.SC_BAD_REQUEST,
                        new ErrorResponse("Bad Request", 400, "account_number and operation are required"));
                return;
            }

            // Get modifier (admin) from session
            SessionData sessionData = (SessionData) req.getAttribute("sessionData");
            long modifiedBy = sessionData.getUserId();

            switch (operation) {
                case "ACTIVATE":
                    adminDAO.changeAccountStatus(accountNumber, "ACTIVE", modifiedBy);
                    break;
                case "INACTIVATE":
                    adminDAO.changeAccountStatus(accountNumber, "INACTIVE", modifiedBy);
                    break;
                case "DELETE":
                    adminDAO.deleteAccount(accountNumber);
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

   

}