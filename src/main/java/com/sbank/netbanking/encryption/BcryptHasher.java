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






































