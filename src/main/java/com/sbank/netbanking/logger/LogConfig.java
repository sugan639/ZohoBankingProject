package com.sbank.netbanking.logger;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.sbank.netbanking.constants.AppConstants;

public class LogConfig {
    public static void configure() {
        try {
            Logger rootLogger = Logger.getLogger("");
            
            // Remove existing handlers (usually console)
            for (Handler handler : rootLogger.getHandlers()) {
                rootLogger.removeHandler(handler);
            }

            FileHandler fileHandler = new FileHandler(AppConstants.LOG_FILE_PATH, AppConstants.LOG_APPEND_MODE);
            fileHandler.setFormatter(new SimpleFormatter());

            rootLogger.addHandler(fileHandler);
            rootLogger.setLevel(AppConstants.LOG_LEVEL);

        } catch (IOException e) {
            System.err.println("Failed to initialize log file:");
            e.printStackTrace();
        }
    }
}
