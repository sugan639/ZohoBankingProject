package com.sbank.netbanking.util;

import com.sbank.netbanking.dto.UserDTO;
import com.sbank.netbanking.model.User;

public class UserMapper {
	
	//Data transfer object for sending selective data from the users table and to mask password.

    public UserDTO toUserDTO(User user) {
        if (user == null) return null;

        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setMobileNumber(user.getMobileNumber());
        dto.setRole(user.getRole());
        
        return dto;
    }
}
