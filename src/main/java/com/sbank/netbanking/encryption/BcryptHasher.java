package com.sbank.netbanking.encryption;


public class BcryptHasher {
	
	public static void main(String[] args) {
		String password = "Sugan@123";
//		
//		String hashedPw = BCrypt.hashpw(password, BCrypt.gensalt());
//		
//		System.out.println(hashedPw);
		
		Boolean isCorrect = BCrypt.checkpw(password, "$2a$10$t.LJis3fSwK3ALDhK0zvuudkRdcQrlk8LEvxujRD8Os46e4UkRSLy");
		
		
		System.out.println(isCorrect);

		
		
		
	}

}




























INSERT INTO branches (
	    branch_id,
	    admin_id,
	    ifsc_code,
	    bank_name,
	    location,
	    created_at,
	    modified_at,
	    modified_by
	) VALUES (
	    900001, -- or any unique branch ID
	    100000001,
	    'SBIN900001',
	    'Sugan Bank',
	    'Salem',
	    UNIX_TIMESTAMP(),
	    UNIX_TIMESTAMP(),
	    100000001
	);









