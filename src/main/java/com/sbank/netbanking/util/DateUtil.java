package com.sbank.netbanking.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.sbank.netbanking.exceptions.TaskException;

public class DateUtil {

    private static final String DATE_FORMAT = "dd-MM-yyyy";

    // Converts "15-05-1990" to epoch millis
    public static long convertDateToEpoch(String dateStr) throws TaskException {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        try {
            Date date = sdf.parse(dateStr);
            return date.getTime();
        } catch (ParseException e) {
            throw new TaskException("Invalid date format. Expected dd-MM-yyyy", e);
        }
    }

    // Converts epoch millis to "15-05-1990"
    public static String convertEpochToDate(long epochMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        return sdf.format(new Date(epochMillis));
    }
}
