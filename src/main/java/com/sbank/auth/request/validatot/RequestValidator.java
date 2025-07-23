package com.sbank.auth.request.validatot;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

public class RequestValidator {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PAN_PATTERN =
            Pattern.compile("^[A-Z]{5}[0-9]{4}[A-Z]$");
    private static final Pattern AADHAR_PATTERN =
            Pattern.compile("^\\d{12}$");
    private static final Pattern MOBILE_PATTERN =
            Pattern.compile("^[6-9]\\d{9}$");
    private static final SimpleDateFormat DOB_FORMAT =
            new SimpleDateFormat("dd-MM-yyyy");

    public static boolean isValid(HttpServletRequest req, StringBuilder errorMessage) {

        String name = req.getParameter("name");
        String email = req.getParameter("email");
        String dob = req.getParameter("dob");
        String pan = req.getParameter("pan_number");
        String aadhar = req.getParameter("aadhar_number");
        String mobile = req.getParameter("mobile_number");
        String address = req.getParameter("address");
        String branchId = req.getParameter("branch_id");
        String role = req.getParameter("role");

        if (name == null || name.trim().isEmpty()) {
            errorMessage.append("Name is required.");
            return false;
        }

        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            errorMessage.append("Invalid email format.");
            return false;
        }

        if (dob == null || !isValidDob(dob)) {
            errorMessage.append("Invalid or underage DOB. Must be at least 18 years old.");
            return false;
        }

        if (pan != null && !PAN_PATTERN.matcher(pan).matches()) {
            errorMessage.append("Invalid PAN format.");
            return false;
        }

        if (aadhar != null && !AADHAR_PATTERN.matcher(aadhar).matches()) {
            errorMessage.append("Invalid Aadhar number.");
            return false;
        }

        if (mobile == null || !MOBILE_PATTERN.matcher(mobile).matches()) {
            errorMessage.append("Invalid mobile number.");
            return false;
        }

        if (address != null && address.trim().length() < 5) {
            errorMessage.append("Address too short.");
            return false;
        }

        if (branchId == null || !branchId.matches("\\d+")) {
            errorMessage.append("Invalid branch ID.");
            return false;
        }

        if (role != null && !role.matches("ADMIN|EMPLOYEE|CUSTOMER")) {
            errorMessage.append("Invalid role.");
            return false;
        }

        return true;
    }

    private static boolean isValidDob(String dob) {
        try {
            DOB_FORMAT.setLenient(false);
            Date birthDate = DOB_FORMAT.parse(dob);
            long ageMillis = System.currentTimeMillis() - birthDate.getTime();
            long ageYears = ageMillis / (1000L * 60 * 60 * 24 * 365);
            return ageYears >= 18;
        } catch (ParseException e) {
            return false;
        }
    }
}
