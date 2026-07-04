package com.bookstore.dao;

import com.bookstore.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class CartDao {
    public Map<Long, Integer> findCart(long userId) throws SQLException {
        Map<Long, Integer> cart = new HashMap<>();
        String sql = "SELECT book_id, quantity FROM cart WHERE user_id = ?";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    cart.put(rs.getLong("book_id"), rs.getInt("quantity"));
                }
            }
        }
        return cart;
    }

    public int countItems(long userId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(quantity), 0) FROM cart WHERE user_id = ?";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public void add(long userId, long bookId, int quantity, int stock) throws SQLException {
        try (Connection connection = DBUtil.getConnection()) {
            int current = findQuantity(connection, userId, bookId);
            if (current > 0) {
                update(connection, userId, bookId, Math.min(current + Math.max(1, quantity), stock));
            } else {
                insert(connection, userId, bookId, Math.min(Math.max(1, quantity), stock));
            }
        }
    }

    public void update(long userId, long bookId, int quantity) throws SQLException {
        if (quantity <= 0) {
            remove(userId, bookId);
            return;
        }
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE cart SET quantity = ? WHERE user_id = ? AND book_id = ?")) {
            statement.setInt(1, quantity);
            statement.setLong(2, userId);
            statement.setLong(3, bookId);
            statement.executeUpdate();
        }
    }

    public void remove(long userId, long bookId) throws SQLException {
        String sql = "DELETE FROM cart WHERE user_id = ? AND book_id = ?";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setLong(2, bookId);
            statement.executeUpdate();
        }
    }

    public void clear(long userId) throws SQLException {
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM cart WHERE user_id = ?")) {
            statement.setLong(1, userId);
            statement.executeUpdate();
        }
    }

    private int findQuantity(Connection connection, long userId, long bookId) throws SQLException {
        String sql = "SELECT quantity FROM cart WHERE user_id = ? AND book_id = ? LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setLong(2, bookId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getInt("quantity") : 0;
            }
        }
    }

    private void insert(Connection connection, long userId, long bookId, int quantity) throws SQLException {
        String sql = "INSERT INTO cart (user_id, book_id, quantity) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setLong(2, bookId);
            statement.setInt(3, quantity);
            statement.executeUpdate();
        }
    }

    private void update(Connection connection, long userId, long bookId, int quantity) throws SQLException {
        String sql = "UPDATE cart SET quantity = ? WHERE user_id = ? AND book_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, quantity);
            statement.setLong(2, userId);
            statement.setLong(3, bookId);
            statement.executeUpdate();
        }
    }
}
