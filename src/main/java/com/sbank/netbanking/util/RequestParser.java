package com.sbank.netbanking.util;

import com.sbank.netbanking.exceptions.TaskException;

public class RequestParser {

	
    public String pathParser(String fullPath) throws TaskException {    // Path parser
        if (fullPath == null || fullPath.isEmpty()) {
            return null;
        }
        int queryIndex = fullPath.indexOf("?");
        return (queryIndex == -1) ? fullPath : fullPath.substring(0, queryIndex);
    }

}


