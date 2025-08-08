package com.sbank.netbanking.util;

import com.sbank.netbanking.constants.AppConstants;

public class BranchUtil {
	
	public String generateIfscCode(String location) {
		String bankName = AppConstants.BANK_NAME;
	    String bankCode = bankName.replaceAll("\\s+", "").toUpperCase().substring(0, Math.min(4, bankName.length()));
	    String locCode = location.replaceAll("\\s+", "").toUpperCase().substring(0, Math.min(2, location.length()));
	    String randomDigits = String.valueOf((int)(Math.random() * 9000) + 1000); // 4 random digits
	    return bankCode + locCode + randomDigits; 
	}


}

