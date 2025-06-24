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
            
            // Remove all existing handlers (e.g., console)
            for (Handler handler : rootLogger.getHandlers()) {
                rootLogger.removeHandler(handler);
            }

            // Ensure directories exist
            String logPath = AppConstants.LOG_FILE_PATH;
            java.nio.file.Path parentDir = java.nio.file.Paths.get(logPath).getParent();
            if (parentDir != null) {
                java.nio.file.Files.createDirectories(parentDir);
            }

            FileHandler fileHandler = new FileHandler(logPath, AppConstants.LOG_APPEND_MODE);
            fileHandler.setFormatter(new SimpleFormatter());
            fileHandler.setLevel(AppConstants.LOG_LEVEL); // ← important
            rootLogger.addHandler(fileHandler);

            rootLogger.setLevel(AppConstants.LOG_LEVEL); // ← required to activate desired level

            
            
        } catch (IOException e) {
            System.err.println("Failed to initialize log file:");
            e.printStackTrace();
        }
    }
}
