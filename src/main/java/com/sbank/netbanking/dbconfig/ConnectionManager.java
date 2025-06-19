
package com.sbank.netbanking.dbconfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.sbank.netbanking.exceptions.ExceptionMessages;
import com.sbank.netbanking.exceptions.TaskException;

public class ConnectionManager {

    private Connection connection;

    // Initiate Connection
    public void initConnection() throws TaskException {
        try {
        	Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(DbConfig.getDbUrl(),
            											  DbConfig.getDbUser(), 
            											  DbConfig.getDbPassword()
            											  );
        } 
        
        catch (SQLException | ClassNotFoundException e) {
            throw new TaskException(ExceptionMessages.CONNECTION_INIT_FAILED, e);
        }
    }

    // Get connection
    public Connection getConnection() {
        return this.connection;
    }

    
    // Stop Connection
    public void stopConnection() throws TaskException {
        if (this.connection != null) {
            
        	try {
                this.connection.close();
            } 
            catch (SQLException e) {
            	throw new TaskException(ExceptionMessages.CONNECTION_CLOSE_FAILED, e);
            }
        }
    }
}