package com.bookstore.dao;

import com.bookstore.model.Book;
import com.bookstore.model.StatItem;
import com.bookstore.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FavoriteDao {
    private static final String FAVORITE_BOOK_SELECT = "SELECT b.book_id, b.title, b.author, b.price, b.description, "
            + "b.stock, b.create_time, c.category_name AS category "
            + "FROM favorites f JOIN books b ON f.book_id = b.book_id "
            + "LEFT JOIN categories c ON b.category_id = c.category_id ";

    public void add(long userId, long bookId) throws SQLException {
        String sql = "INSERT IGNORE INTO favorites (user_id, book_id) VALUES (?, ?)";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setLong(2, bookId);
            statement.executeUpdate();
        }
    }

    public void remove(long userId, long bookId) throws SQLException {
        String sql = "DELETE FROM favorites WHERE user_id = ? AND book_id = ?";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setLong(2, bookId);
            statement.executeUpdate();
        }
    }

    public List<Book> findByUser(long userId) throws SQLException {
        String sql = FAVORITE_BOOK_SELECT + "WHERE f.user_id = ? ORDER BY f.create_time DESC, f.favorite_id DESC";
        List<Book> books = new ArrayList<>();
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    books.add(mapBook(rs));
                }
            }
        }
        return books;
    }

    public Set<Long> findFavoriteBookIds(long userId) throws SQLException {
        Set<Long> ids = new HashSet<>();
        String sql = "SELECT book_id FROM favorites WHERE user_id = ?";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getLong("book_id"));
                }
            }
        }
        return ids;
    }

    public long countByUser(long userId) throws SQLException {
        return queryLong("SELECT COUNT(*) FROM favorites WHERE user_id = ?", userId);
    }

    public long countAll() throws SQLException {
        return queryLong("SELECT COUNT(*) FROM favorites");
    }

    public List<StatItem> topFavoritedBooks(int limit) throws SQLException {
        String sql = "SELECT b.title AS label, COALESCE(c.category_name, '') AS meta, COUNT(*) AS value "
                + "FROM favorites f JOIN books b ON f.book_id = b.book_id "
                + "LEFT JOIN categories c ON b.category_id = c.category_id "
                + "GROUP BY b.book_id, b.title, c.category_name ORDER BY value DESC LIMIT ?";
        List<StatItem> items = new ArrayList<>();
        long max = 0;
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    long value = rs.getLong("value");
                    max = Math.max(max, value);
                    items.add(new StatItem(rs.getString("label"), rs.getString("meta"), value, 0));
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
