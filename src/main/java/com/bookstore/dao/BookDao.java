package com.bookstore.dao;

import com.bookstore.model.Book;
import com.bookstore.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookDao {
    private static final String BOOK_SELECT = "SELECT b.book_id, b.title, b.author, b.price, b.description, "
            + "b.stock, b.create_time, c.category_name AS category "
            + "FROM books b LEFT JOIN categories c ON b.category_id = c.category_id";

    public List<Book> findBooks(String category, String keyword) throws SQLException {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BOOK_SELECT + " WHERE 1=1");
        if (category != null && !category.isBlank()) {
            sql.append(" AND c.category_name = ?");
            params.add(category);
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (LOWER(b.title) LIKE ? OR LOWER(b.author) LIKE ?)");
            String like = "%" + keyword.trim().toLowerCase() + "%";
            params.add(like);
            params.add(like);
        }
        sql.append(" ORDER BY b.book_id ASC");
        return queryBooks(sql.toString(), params);
    }

    public List<Book> findFeatured() throws SQLException {
        return queryBooks(BOOK_SELECT + " ORDER BY b.book_id ASC LIMIT 3", List.of());
    }

    public List<Book> findAll() throws SQLException {
        return queryBooks(BOOK_SELECT + " ORDER BY b.book_id ASC", List.of());
    }

    public List<String> findCategories() throws SQLException {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT category_name FROM categories ORDER BY category_id ASC";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                categories.add(rs.getString("category_name"));
            }
        }
        return categories;
    }

    public Optional<Book> findById(long id) throws SQLException {
        List<Book> books = queryBooks(BOOK_SELECT + " WHERE b.book_id = ?", List.of(id));
        return books.stream().findFirst();
    }

    public long countAll() throws SQLException {
        return queryLong("SELECT COUNT(*) FROM books");
    }

    public long countLowStock() throws SQLException {
        return queryLong("SELECT COUNT(*) FROM books WHERE stock <= 5");
    }

    public void create(Book book) throws SQLException {
        String sql = "INSERT INTO books (title, author, price, description, category_id, stock) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, book.getTitle());
            statement.setString(2, book.getAuthor());
            statement.setBigDecimal(3, book.getPrice());
            statement.setString(4, book.getDescription());
            statement.setLong(5, ensureCategoryId(connection, book.getCategory()));
            statement.setInt(6, book.getStock());
            statement.executeUpdate();
        }
    }

    public void update(Book book) throws SQLException {
        String sql = "UPDATE books SET title = ?, author = ?, price = ?, description = ?, category_id = ?, stock = ? "
                + "WHERE book_id = ?";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, book.getTitle());
            statement.setString(2, book.getAuthor());
            statement.setBigDecimal(3, book.getPrice());
            statement.setString(4, book.getDescription());
            statement.setLong(5, ensureCategoryId(connection, book.getCategory()));
            statement.setInt(6, book.getStock());
            statement.setLong(7, book.getId());
            statement.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM books WHERE book_id = ?")) {
            statement.setLong(1, id);
            statement.executeUpdate();
        }
    }

    private List<Book> queryBooks(String sql, List<?> params) throws SQLException {
        List<Book> books = new ArrayList<>();
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                statement.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    books.add(mapBook(rs));
                }
            }
        }
        return books;
    }

    private long queryLong(String sql) throws SQLException {
        try (Connection connection = DBUtil.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            return rs.next() ? rs.getLong(1) : 0;
        }
    }

    private long ensureCategoryId(Connection connection, String categoryName) throws SQLException {
        String normalized = categoryName == null || categoryName.isBlank() ? "Uncategorized" : categoryName.trim();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT category_id FROM categories WHERE category_name = ?")) {
            statement.setString(1, normalized);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("category_id");
                }
            }
        }
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO categories (category_name, description) VALUES (?, '')",
                Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, normalized);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }
        throw new SQLException("Failed to create category: " + normalized);
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
        book.setCoverColor(coverColorFor(book.getCategory()));
        book.setFeatured(book.getId() <= 3);
        Timestamp createdAt = rs.getTimestamp("create_time");
        if (createdAt != null) {
            book.setCreatedAt(createdAt.toLocalDateTime());
        }
        return book;
    }

    private String coverColorFor(String category) {
        switch (category) {
            case "Computer Science":
            case "编程技术":
                return "linear-gradient(145deg, #8fb7ff, #4d73c8)";
            case "Literature":
            case "文学小说":
                return "linear-gradient(145deg, #f5b8c7, #c95d76)";
            case "Economics":
            case "商业管理":
                return "linear-gradient(145deg, #b8d7f0, #5d93c9)";
            case "History":
                return "linear-gradient(145deg, #f0d9a6, #c69853)";
            case "Science":
                return "linear-gradient(145deg, #9dd8c8, #3f9d87)";
            default:
                return "linear-gradient(145deg, #f3b27c, #d96f45)";
        }
    }
}
