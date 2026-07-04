package com.bookstore.dao;

import com.bookstore.model.Book;
import com.bookstore.model.CartItem;
import com.bookstore.model.CartSummary;
import com.bookstore.model.Order;
import com.bookstore.model.OrderItem;
import com.bookstore.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class OrderDao {
    public Order createOrder(long userId, CartSummary summary, String address, String paymentMethod) throws SQLException {
        if (summary.getItems().isEmpty()) {
            throw new SQLException("Cart is empty");
        }
        try (Connection connection = DBUtil.getConnection()) {
            boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                long orderId = insertOrder(connection, userId, summary, address, paymentMethod);
                String orderNo = generateOrderNo(orderId);
                updateOrderNo(connection, orderId, orderNo);
                insertOrderItems(connection, orderId, summary);
                reduceStock(connection, summary);
                clearCart(connection, userId);
                connection.commit();
                Order order = new Order();
                order.setOrderId(orderId);
                order.setOrderNo(orderNo);
                order.setTotalPrice(summary.getTotalPrice());
                order.setStatus("Paid");
                order.setAddress(address);
                order.setPaymentMethod(paymentMethod);
                return order;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(autoCommit);
            }
        }
    }

    public List<Order> findByUser(long userId) throws SQLException {
        String sql = "SELECT o.order_id, COALESCE(o.order_no, '') AS order_no, o.user_id, u.username, o.total_price, o.status, o.address, "
                + "COALESCE(o.payment_method, '') AS payment_method, o.create_time "
                + "FROM orders o JOIN users u ON o.user_id = u.user_id "
                + "WHERE o.user_id = ? ORDER BY o.create_time DESC, o.order_id DESC";
        List<Order> orders = queryOrders(sql, userId);
        fillItems(orders);
        return orders;
    }

    public Order findById(long orderId) throws SQLException {
        String sql = "SELECT o.order_id, COALESCE(o.order_no, '') AS order_no, o.user_id, u.username, o.total_price, o.status, o.address, "
                + "COALESCE(o.payment_method, '') AS payment_method, o.create_time "
                + "FROM orders o JOIN users u ON o.user_id = u.user_id WHERE o.order_id = ?";
        List<Order> orders = queryOrders(sql, orderId);
        fillItems(orders);
        return orders.isEmpty() ? null : orders.get(0);
    }

    public Order findByIdForUser(long orderId, long userId) throws SQLException {
        String sql = "SELECT o.order_id, COALESCE(o.order_no, '') AS order_no, o.user_id, u.username, o.total_price, o.status, o.address, "
                + "COALESCE(o.payment_method, '') AS payment_method, o.create_time "
                + "FROM orders o JOIN users u ON o.user_id = u.user_id WHERE o.order_id = ? AND o.user_id = ?";
        List<Order> orders = queryOrders(sql, orderId, userId);
        fillItems(orders);
        return orders.isEmpty() ? null : orders.get(0);
    }

    public List<Order> findAll(String status) throws SQLException {
        return findAll(status, "");
    }

    public List<Order> findAll(String status, String keyword) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT o.order_id, COALESCE(o.order_no, '') AS order_no, o.user_id, u.username, o.total_price, o.status, "
                + "o.address, COALESCE(o.payment_method, '') AS payment_method, o.create_time "
                + "FROM orders o JOIN users u ON o.user_id = u.user_id WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (status != null && !status.isBlank()) {
            sql.append(" AND o.status = ?");
            params.add(status);
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (LOWER(COALESCE(o.order_no, '')) LIKE ? OR LOWER(u.username) LIKE ?)");
            String like = "%" + keyword.trim().toLowerCase() + "%";
            params.add(like);
            params.add(like);
        }
        sql.append(" ORDER BY o.create_time DESC, o.order_id DESC");
        List<Order> orders = queryOrders(sql.toString(), params.toArray());
        fillItems(orders);
        return orders;
    }

    public long countByUser(long userId) throws SQLException {
        return queryLong("SELECT COUNT(*) FROM orders WHERE user_id = ?", userId);
    }

    public long countAll() throws SQLException {
        return queryLong("SELECT COUNT(*) FROM orders");
    }

    public java.math.BigDecimal totalRevenue() throws SQLException {
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COALESCE(SUM(total_price), 0) FROM orders WHERE status <> 'Cancelled'");
             ResultSet rs = statement.executeQuery()) {
            return rs.next() ? rs.getBigDecimal(1) : java.math.BigDecimal.ZERO;
        }
    }

    public void updateStatus(long orderId, String status) throws SQLException {
        String sql = "UPDATE orders SET status = ? WHERE order_id = ?";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, normalizeStatus(status));
            statement.setLong(2, orderId);
            statement.executeUpdate();
        }
    }

    private long insertOrder(Connection connection, long userId, CartSummary summary, String address, String paymentMethod)
            throws SQLException {
        String sql = "INSERT INTO orders (user_id, total_price, status, address, payment_method) "
                + "VALUES (?, ?, 'Paid', ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, userId);
            statement.setBigDecimal(2, summary.getTotalPrice());
            statement.setString(3, address);
            statement.setString(4, paymentMethod);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }
        throw new SQLException("Failed to create order");
    }

    private void updateOrderNo(Connection connection, long orderId, String orderNo) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE orders SET order_no = ? WHERE order_id = ?")) {
            statement.setString(1, orderNo);
            statement.setLong(2, orderId);
            statement.executeUpdate();
        }
    }

    private String generateOrderNo(long orderId) {
        return "BN" + java.time.LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-" + String.format("%06d", orderId);
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

    private List<Order> queryOrders(String sql, Object... params) throws SQLException {
        List<Order> orders = new ArrayList<>();
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapOrder(rs));
                }
            }
        }
        return orders;
    }

    private long queryLong(String sql, Object... params) throws SQLException {
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0;
            }
        }
    }

    private void fillItems(List<Order> orders) throws SQLException {
        for (Order order : orders) {
            order.setItems(findItems(order.getOrderId()));
        }
    }

    private List<OrderItem> findItems(long orderId) throws SQLException {
        String sql = "SELECT oi.item_id, oi.order_id, oi.quantity, oi.price, "
                + "b.book_id, b.title, b.author, b.description, b.stock, c.category_name "
                + "FROM order_items oi "
                + "JOIN books b ON oi.book_id = b.book_id "
                + "LEFT JOIN categories c ON b.category_id = c.category_id "
                + "WHERE oi.order_id = ? ORDER BY oi.item_id ASC";
        List<OrderItem> items = new ArrayList<>();
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, orderId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    items.add(mapOrderItem(rs));
                }
            }
        }
        return items;
    }

    private Order mapOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setOrderId(rs.getLong("order_id"));
        String orderNo = rs.getString("order_no");
        order.setOrderNo(orderNo == null || orderNo.isBlank() ? "#" + order.getOrderId() : orderNo);
        order.setUserId(rs.getLong("user_id"));
        order.setUsername(rs.getString("username"));
        order.setTotalPrice(rs.getBigDecimal("total_price"));
        order.setStatus(rs.getString("status"));
        order.setAddress(rs.getString("address"));
        order.setPaymentMethod(rs.getString("payment_method"));
        Timestamp createdAt = rs.getTimestamp("create_time");
        if (createdAt != null) {
            order.setCreatedAt(createdAt.toLocalDateTime());
        }
        return order;
    }

    private OrderItem mapOrderItem(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setId(rs.getLong("book_id"));
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));
        book.setCategory(rs.getString("category_name"));
        book.setDescription(rs.getString("description"));
        book.setStock(rs.getInt("stock"));
        book.setCoverColor(BookDao.coverColorFor(book.getCategory()));

        OrderItem item = new OrderItem();
        item.setItemId(rs.getLong("item_id"));
        item.setOrderId(rs.getLong("order_id"));
        item.setBook(book);
        item.setQuantity(rs.getInt("quantity"));
        item.setPrice(rs.getBigDecimal("price"));
        return item;
    }

    private String normalizeStatus(String status) {
        if ("Shipped".equals(status) || "Completed".equals(status) || "Cancelled".equals(status)) {
            return status;
        }
        return "Paid";
    }
}
