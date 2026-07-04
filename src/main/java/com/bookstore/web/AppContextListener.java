package com.bookstore.web;

import com.bookstore.util.DBUtil;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.sql.SQLException;

@WebListener
public class AppContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            DBUtil.initialize();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to initialize bookstore database", e);
        }
    }
}

