package com.bookstore.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

public final class DBUtil {
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/online_bookstore"
            + "?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai"
            + "&useSSL=false&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "123456";

    private DBUtil() {
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL driver not found", e);
        }
        String url = System.getProperty("bookstore.jdbc.url", DEFAULT_URL);
        String user = System.getProperty("bookstore.jdbc.user", DEFAULT_USER);
        String password = System.getProperty("bookstore.jdbc.password", DEFAULT_PASSWORD);
        return DriverManager.getConnection(url, user, password);
    }

    public static void initialize() throws SQLException {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            for (String sql : loadSqlScript().split(";")) {
                String command = sql.trim();
                if (!command.isEmpty()) {
                    statement.execute(command);
                }
            }
        }
    }

    private static String loadSqlScript() throws SQLException {
        try (InputStream inputStream = DBUtil.class.getClassLoader().getResourceAsStream("database.sql")) {
            if (inputStream == null) {
                throw new SQLException("database.sql not found in classpath");
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                return reader.lines()
                        .filter(line -> !line.trim().startsWith("--"))
                        .collect(Collectors.joining(System.lineSeparator()));
            }
        } catch (IOException e) {
            throw new SQLException("Failed to load database.sql", e);
        }
    }
}
