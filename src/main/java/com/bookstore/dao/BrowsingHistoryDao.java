package com.bookstore.dao;

import com.bookstore.model.Book;
import com.bookstore.model.BrowsingRecord;
import com.bookstore.model.StatItem;
import com.bookstore.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class BrowsingHistoryDao {
    public void recordViews(long userId, List<Book> books) throws SQLException {
        if (books == null || books.isEmpty()) {
            return;
        }
        String sql = "INSERT INTO browsing_history (user_id, book_id, view_count, last_viewed) "
                + "VALUES (?, ?, 1, NOW()) "
                + "ON DUPLICATE KEY UPDATE view_count = view_count + 1, last_viewed = NOW()";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Book book : books) {
                statement.setLong(1, userId);
                statement.setLong(2, book.getId());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    public List<BrowsingRecord> findRecentByUser(long userId, int limit) throws SQLException {
        String sql = "SELECT h.view_count, h.last_viewed, b.book_id, b.title, b.author, b.price, b.description, "
                + "b.stock, b.create_time, c.category_name AS category "
                + "FROM browsing_history h JOIN books b ON h.book_id = b.book_id "
                + "LEFT JOIN categories c ON b.category_id = c.category_id "
                + "WHERE h.user_id = ? ORDER BY h.last_viewed DESC LIMIT ?";
        List<BrowsingRecord> records = new ArrayList<>();
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setInt(2, limit);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    BrowsingRecord record = new BrowsingRecord();
                    record.setBook(mapBook(rs));
                    record.setViewCount(rs.getInt("view_count"));
                    Timestamp lastViewed = rs.getTimestamp("last_viewed");
                    if (lastViewed != null) {
                        record.setLastViewed(lastViewed.toLocalDateTime());
                    }
                    records.add(record);
                }
            }
        }
        return records;
    }

    public List<StatItem> categoryStatsByUser(long userId) throws SQLException {
        String sql = "SELECT COALESCE(c.category_name, 'Uncategorized') AS label, SUM(h.view_count) AS value "
                + "FROM browsing_history h JOIN books b ON h.book_id = b.book_id "
                + "LEFT JOIN categories c ON b.category_id = c.category_id "
                + "WHERE h.user_id = ? GROUP BY label ORDER BY value DESC";
        return queryStats(sql, userId);
    }

    public List<StatItem> topViewedBooks(int limit) throws SQLException {
        String sql = "SELECT b.title AS label, COALESCE(c.category_name, '') AS meta, SUM(h.view_count) AS value "
                + "FROM browsing_history h JOIN books b ON h.book_id = b.book_id "
                + "LEFT JOIN categories c ON b.category_id = c.category_id "
                + "GROUP BY b.book_id, b.title, c.category_name ORDER BY value DESC LIMIT ?";
        return queryStats(sql, limit);
    }

    public long countBooksByUser(long userId) throws SQLException {
        return queryLong("SELECT COUNT(*) FROM browsing_history WHERE user_id = ?", userId);
    }

    public long countViewsByUser(long userId) throws SQLException {
        return queryLong("SELECT COALESCE(SUM(view_count), 0) FROM browsing_history WHERE user_id = ?", userId);
    }

    public long countAllViews() throws SQLException {
        return queryLong("SELECT COALESCE(SUM(view_count), 0) FROM browsing_history");
    }

    private List<StatItem> queryStats(String sql, Object... params) throws SQLException {
        List<StatItem> items = new ArrayList<>();
        long max = 0;
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String meta;
                    try {
                        meta = rs.getString("meta");
                    } catch (SQLException ignored) {
                        meta = "";
                    }
                    long value = rs.getLong("value");
                    max = Math.max(max, value);
                    items.add(new StatItem(rs.getString("label"), meta == null ? "" : meta, value, 0));
                }
            }
        }
        for (StatItem item : items) {
            item.setMaxValue(max);
        }
        return items;
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

    private Book mapBook(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setId(rs.getLong("book_id"));
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));
        book.setCategory(rs.getString("category") == null ? "Uncategorized" : rs.getString("category"));
        book.setPrice(rs.getBigDecimal("price"));
        book.setStock(rs.getInt("stock"));
        book.setDescription(rs.getString("description") == null ? "" : rs.getString("description"));
        book.setCoverColor(BookDao.coverColorFor(book.getCategory()));
        book.setFeatured(book.getId() <= 3);
        Timestamp createdAt = rs.getTimestamp("create_time");
        if (createdAt != null) {
            book.setCreatedAt(createdAt.toLocalDateTime());
        }
        return book;
    }
}
