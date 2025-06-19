package com.sbank.netbanking.util;

import java.lang.reflect.Field;
import java.util.logging.Logger;

import org.json.JSONObject;

import com.sbank.netbanking.exceptions.ExceptionMessages;
import com.sbank.netbanking.exceptions.TaskException;

public class PojoJsonConverter {

    public JSONObject pojoToJson(Object pojo) throws TaskException {
    	
        JSONObject json = new JSONObject();
        final Logger LOGGER = Logger.getLogger("BankAppLogger");
        
        
        if (pojo == null) {
       
            LOGGER.log( null, "Null POJO object");
            throw new TaskException(ExceptionMessages.POJO_NULL_EXCEPTION);
           
        }

        try {
            Class<?> clazz = pojo.getClass();
            Field[] fields = clazz.getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true); // allow private fields

                Object value = field.get(pojo);

                // You can skip fields like "password" here if needed
                if (field.getName().equalsIgnoreCase("password")) {
                    continue;
                }

                if (value != null) {
                    json.put(field.getName(), value.toString());
                } else {
                    json.put(field.getName(), JSONObject.NULL);
                }
            }

        } catch (IllegalAccessException e) {
            
            throw new TaskException(ExceptionMessages.POJO_TO_JSON_CONVERSION_FAILED,e);
        }

        return json;
    }
}


