package com.megacitycab.mega_city_cab.config;


import org.apache.commons.dbcp2.BasicDataSource;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class MyListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        System.out.println("Application Servlet Context Was Created");

        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/mcc");
        dataSource.setUsername("gihan");
        dataSource.setPassword("1234");
        servletContextEvent.getServletContext().setAttribute("ds", dataSource);
    }
    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        System.out.println("Application Servlet Context Was Destroyed");
    }
}
