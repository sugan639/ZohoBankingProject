package com.sbank.netbanking.dbconfig;

import com.sbank.netbanking.exceptions.TaskException;

public final class DbConfig {

//    private static final String CONFIG_PATH = "/db.properties";
//    private static volatile Properties properties = null;
//    private static final Object lock = new Object();

    private DbConfig() {
        // Prevent instantiation
    }

//    private static void loadProperties() throws TaskException {
//        if (properties != null) return;
//
//        synchronized (lock) {
//            if (properties != null) return;
//
//            Properties tempProps = new Properties();
//            try (InputStream fis = new FileInputStream(CONFIG_PATH)) {
//                tempProps.load(fis);
//                properties = tempProps;
//            } 
//            		catch (IOException e) {
//                throw new TaskException(
//                    ExceptionMessages.DB_CREDENTIALS_NOT_FOUND + CONFIG_PATH, e);
//            }
//        }
//    }

    public static String getDbUrl() throws TaskException {
       // loadProperties();
        return "jdbc:mysql://localhost:3306/bankDB";
    }

    public static String getDbUser() throws TaskException {
       // loadProperties();
        return "root";
    }

    public static String getDbPassword() throws TaskException {
       // loadProperties();
        return "sugan@123";
    }
//
//    private static String get(String key) throws TaskException {
//        String value = properties.getProperty(key);
//        if (value == null || value.isEmpty()) {
//            throw new TaskException(ExceptionMessages.DB_KEY_MISSING);
//        }
//        return value;
//    }
}