package com.sbank.netbanking.logger;


import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class AppInitializer implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
    	final Logger logger = Logger.getLogger(AppInitializer.class.getName());

        LogConfig.configure(); // MUST be called
        logger.info("Logging initialized.");
        System.out.println("Server refreshed.");
    }
}
