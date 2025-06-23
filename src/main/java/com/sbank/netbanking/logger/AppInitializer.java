package com.sbank.netbanking.logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class AppInitializer implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LogConfig.configure(); // MUST be called
        System.out.println("Logging initialized.");
    }
}
