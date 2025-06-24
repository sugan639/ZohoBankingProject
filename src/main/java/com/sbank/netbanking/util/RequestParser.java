package com.sbank.netbanking.util;

import java.util.HashMap;
import java.util.Map;

import com.sbank.netbanking.exceptions.TaskException;

public class RequestParser {

	
    public String pathParser(String fullPath) throws TaskException {    // Path parser
        if (fullPath == null || fullPath.isEmpty()) {
            return null;
        }
        int queryIndex = fullPath.indexOf("?");
        return (queryIndex == -1) ? fullPath : fullPath.substring(0, queryIndex);
    }


    public Map<String, String> paramParser(String fullPath) {	// Parameter parser
        Map<String, String> paramMap = new HashMap<>();

        if (fullPath == null || !fullPath.contains("?")) {
            return paramMap;  
        }

        String queryString = fullPath.substring(fullPath.indexOf("?") + 1);

        String[] pairs = queryString.split("&");
        
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                paramMap.put(keyValue[0], keyValue[1]);
            } else if (keyValue.length == 1) {
                paramMap.put(keyValue[0], "");
            }
        }

        return paramMap;
    }
}


