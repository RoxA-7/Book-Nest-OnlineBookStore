package com.bookstore.dao;

import com.bookstore.model.User;
import com.bookstore.util.DBUtil;
import com.bookstore.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

public class UserDao {
    public Optional<User> findByUsername(String username) throws SQLException {
        String sql = "SELECT user_id, username, password, email, role, create_time FROM users WHERE username = ?";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<User> authenticate(String username, String password) throws SQLException {
        Optional<User> user = findByUsername(username);
        if (user.isPresent()) {
            String storedPassword = user.get().getPasswordHash();
            if (storedPassword.equals(password) || storedPassword.equals(PasswordUtil.sha256(password))) {
                return user;
            }
        }
        return Optional.empty();
    }

    public void create(String username, String password, String email) throws SQLException {
        String sql = "INSERT INTO users (username, password, email, role) VALUES (?, ?, ?, 'user')";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, PasswordUtil.sha256(password));
            statement.setString(3, email);
            statement.executeUpdate();
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password"));
        user.setEmail(rs.getString("email"));
        user.setRole(rs.getString("role"));
        Timestamp createdAt = rs.getTimestamp("create_time");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }
        return user;
    }
}
