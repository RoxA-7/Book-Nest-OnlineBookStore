package com.bookstore.dao;

import com.bookstore.model.CartItem;
import com.bookstore.model.CartSummary;
import com.bookstore.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class OrderDao {
    public long createOrder(long userId, CartSummary summary) throws SQLException {
        if (summary.getItems().isEmpty()) {
            throw new SQLException("Cart is empty");
        }
        try (Connection connection = DBUtil.getConnection()) {
            boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                long orderId = insertOrder(connection, userId, summary);
                insertOrderItems(connection, orderId, summary);
                reduceStock(connection, summary);
                clearCart(connection, userId);
                connection.commit();
                return orderId;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(autoCommit);
            }
        }
    }

    private long insertOrder(Connection connection, long userId, CartSummary summary) throws SQLException {
        String sql = "INSERT INTO orders (user_id, total_price, status, address) VALUES (?, ?, 'Paid', 'Online checkout')";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, userId);
            statement.setBigDecimal(2, summary.getTotalPrice());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }
        throw new SQLException("Failed to create order");
    }

    private void insertOrderItems(Connection connection, long orderId, CartSummary summary) throws SQLException {
        String sql = "INSERT INTO order_items (order_id, book_id, quantity, price) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (CartItem item : summary.getItems()) {
                statement.setLong(1, orderId);
                statement.setLong(2, item.getBook().getId());
                statement.setInt(3, item.getQuantity());
                statement.setBigDecimal(4, item.getBook().getPrice());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void clearCart(Connection connection, long userId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM cart WHERE user_id = ?")) {
            statement.setLong(1, userId);
            statement.executeUpdate();
        }
    }

    private void reduceStock(Connection connection, CartSummary summary) throws SQLException {
        String sql = "UPDATE books SET stock = GREATEST(stock - ?, 0) WHERE book_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (CartItem item : summary.getItems()) {
                statement.setInt(1, item.getQuantity());
                statement.setLong(2, item.getBook().getId());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }
}
