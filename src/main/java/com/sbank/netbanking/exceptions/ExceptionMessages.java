package com.sbank.netbanking.exceptions;


public class ExceptionMessages {

		// Error messages
	    public static String NULL_INPUT_ERROR = "Input strings are null";
	    public static String LENGTH_ERROR = "String length is very less than required length!";
	    public static String NEGATIVE_VALUE_ERROR = "Index or length cannot be negative";
	    public static String EMPTY_STRING_ERROR = "Input string cannot be empty";
	    public static String EMPTY_SEPARATOR_ERROR = "Separator cannot be empty";
	    public static String EMPTY_TARGET_ERROR = "Target string cannot be empty";
	    public static String EMPTY_CHAR_ERROR = "Character cannot be empty";
	    public static String LENGTH_INDEX_CONFLICT = "Start index must be less than end index";
	    
	    public static String INDEX_OUT_OF_BOUND = "Index out of bound";
	    public static String INVALID_POSITION = "Invalid position";
	    public static String ELEMENT_NOT_FOUND = "Element not found";
	    
	    public static String KEY_NOT_EXISTS = "Key not exists! ";
	    public static String EMPTY_MAP_ERROR = "Map should not be empty! ";
	    public static String HTML_EMPTY_ERROR= "No HTML tags found in the input";
	    public static String REGEX_ERROR="Unable to compile regex";
	    public static String DB_CREDENTIALS_NOT_FOUND = "DB credentials not found!";
	    public static String DB_KEY_MISSING = "DB Key not found";
	    public static String CONNECTION_INIT_FAILED = "Connection initiation failed";
	    public static String CONNECTION_CLOSE_FAILED =  "Connection closing failed";
		public static String DATABASE_CONNECTION_FAILED = "Database connection failed.";
		public static String USERDATA_UPDATE_FAILED = "Unable to update user data";
		public static String WRONG_ACTION = "Unable to make the transaction.";
		public static String TRANSACTION_FAILED = "Unable to make the transaction.";

	    public static String USERDATA_RETRIEVAL_FAILED = "User data retrieval failed";
	    public static String REQUEST_PARSE_TO_JSON_FAILED =  "Request parsing to JSON failed";
	    public static String POJO_NULL_EXCEPTION = "POJO object is NULL";
	    public static String POJO_TO_JSON_CONVERSION_FAILED = "Pojo to json conversion failed";
	    public static String RESPONSE_WRITER_FAILED = "Print writer failed to write in response object";

	    public static String LONG_OBJECT_CONVERSION_ERROR = "Invalid string format for Long object conversion: ";
	    public static String DATA_NOT_FOUND_IN_JSON = "Required data not found on JSON object";
	    public static String SESSION_ID_RETRIEVAL_FAILED = "Session data retrieval failed!";


	    public static String SESSION_DATA_INSERT_FAILED="Failed to insert session data in database!";
	    public static String SESSION_DATA_DELETION_FAILED= "Failed to delete session for userId: ";
	    public static String NULL_SESSIONID_ERROR="Session ID is null. Cannot delete DB session.";
	    public static String USER_SESSION_DATA_NOT_FOUND = "SessionData not found in session. Cannot delete DB session.";
	    public static String DB_SESSION_DATA_NOT_FOUND = "No session found in DB to delete for user ID: " ;
	    public static String ROW_INSERTION_FAILED = "Failed to create session. No rows inserted.";


}
