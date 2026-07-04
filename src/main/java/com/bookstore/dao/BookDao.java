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
            + "b.stock, b.create_time, "
            + "COALESCE(GROUP_CONCAT(DISTINCT c.category_name ORDER BY c.category_id SEPARATOR ', '), legacy.category_name, 'Uncategorized') AS category "
            + "FROM books b "
            + "LEFT JOIN categories legacy ON b.category_id = legacy.category_id "
            + "LEFT JOIN book_categories bc ON b.book_id = bc.book_id "
            + "LEFT JOIN categories c ON bc.category_id = c.category_id";
    private static final String BOOK_GROUP_BY = " GROUP BY b.book_id, b.title, b.author, b.price, b.description, b.stock, b.create_time, legacy.category_name";

    public List<Book> findBooks(String category, String keyword) throws SQLException {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BOOK_SELECT + " WHERE 1=1");
        if (category != null && !category.isBlank()) {
            sql.append(" AND EXISTS (SELECT 1 FROM book_categories fbc JOIN categories fc ON fbc.category_id = fc.category_id "
                    + "WHERE fbc.book_id = b.book_id AND fc.category_name = ?)");
            params.add(category);
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (LOWER(b.title) LIKE ? OR LOWER(b.author) LIKE ?)");
            String like = "%" + keyword.trim().toLowerCase() + "%";
            params.add(like);
            params.add(like);
        }
        sql.append(BOOK_GROUP_BY).append(" ORDER BY b.book_id ASC");
        return queryBooks(sql.toString(), params);
    }

    public List<Book> findFeatured() throws SQLException {
        return queryBooks(BOOK_SELECT + BOOK_GROUP_BY + " ORDER BY b.book_id ASC LIMIT 3", List.of());
    }

    public List<Book> findAll() throws SQLException {
        return queryBooks(BOOK_SELECT + BOOK_GROUP_BY + " ORDER BY b.book_id ASC", List.of());
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
        List<Book> books = queryBooks(BOOK_SELECT + " WHERE b.book_id = ?" + BOOK_GROUP_BY, List.of(id));
        return books.stream().findFirst();
    }

    public List<Book> findRelated(String category, long excludedBookId, int limit) throws SQLException {
        String primaryCategory = firstCategory(category);
        String sql = BOOK_SELECT + " WHERE b.book_id <> ? "
                + "AND EXISTS (SELECT 1 FROM book_categories fbc JOIN categories fc ON fbc.category_id = fc.category_id "
                + "WHERE fbc.book_id = b.book_id AND fc.category_name = ?) "
                + BOOK_GROUP_BY + " ORDER BY b.book_id ASC LIMIT ?";
        return queryBooks(sql, List.of(excludedBookId, primaryCategory, limit));
    }

    public long countCategories() throws SQLException {
        return queryLong("SELECT COUNT(*) FROM categories");
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
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            long primaryCategoryId = ensureCategoryId(connection, firstCategory(book.getCategory()));
            statement.setString(1, book.getTitle());
            statement.setString(2, book.getAuthor());
            statement.setBigDecimal(3, book.getPrice());
            statement.setString(4, book.getDescription());
            statement.setLong(5, primaryCategoryId);
            statement.setInt(6, book.getStock());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    syncBookCategories(connection, keys.getLong(1), book.getCategory());
                }
            }
        }
    }

    public void createCategory(String categoryName, String description) throws SQLException {
        String normalized = categoryName == null || categoryName.isBlank() ? "" : categoryName.trim();
        if (normalized.isEmpty()) {
            throw new SQLException("Category name is required");
        }
        String sql = "INSERT INTO categories (category_name, description) VALUES (?, ?) "
                + "ON DUPLICATE KEY UPDATE description = VALUES(description)";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, normalized);
            statement.setString(2, description == null ? "" : description.trim());
            statement.executeUpdate();
        }
    }

    public void update(Book book) throws SQLException {
        String sql = "UPDATE books SET title = ?, author = ?, price = ?, description = ?, category_id = ?, stock = ? "
                + "WHERE book_id = ?";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            long primaryCategoryId = ensureCategoryId(connection, firstCategory(book.getCategory()));
            statement.setString(1, book.getTitle());
            statement.setString(2, book.getAuthor());
            statement.setBigDecimal(3, book.getPrice());
            statement.setString(4, book.getDescription());
            statement.setLong(5, primaryCategoryId);
            statement.setInt(6, book.getStock());
            statement.setLong(7, book.getId());
            statement.executeUpdate();
            syncBookCategories(connection, book.getId(), book.getCategory());
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

    private void syncBookCategories(Connection connection, long bookId, String categoryText) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM book_categories WHERE book_id = ?")) {
            statement.setLong(1, bookId);
            statement.executeUpdate();
        }
        String sql = "INSERT IGNORE INTO book_categories (book_id, category_id) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (String category : splitCategories(categoryText)) {
                statement.setLong(1, bookId);
                statement.setLong(2, ensureCategoryId(connection, category));
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private List<String> splitCategories(String categoryText) {
        String normalized = categoryText == null || categoryText.isBlank() ? "Uncategorized" : categoryText;
        List<String> categories = new ArrayList<>();
        for (String part : normalized.split("[,，/、]")) {
            String category = part.trim();
            if (!category.isEmpty() && !categories.contains(category)) {
                categories.add(category);
            }
        }
        if (categories.isEmpty()) {
            categories.add("Uncategorized");
        }
        return categories;
    }

    private String firstCategory(String categoryText) {
        return splitCategories(categoryText).get(0);
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
        book.setCoverColor(coverColorFor(firstCategory(book.getCategory())));
        book.setFeatured(book.getId() <= 3);
        Timestamp createdAt = rs.getTimestamp("create_time");
        if (createdAt != null) {
            book.setCreatedAt(createdAt.toLocalDateTime());
        }
        return book;
    }

    public static String coverColorFor(String category) {
        switch (category) {
            case "Computer Science":
            case "编程技术":
                return "linear-gradient(145deg, #9ca7ad, #56616a)";
            case "Literature":
            case "文学小说":
                return "linear-gradient(145deg, #b7a89b, #786b60)";
            case "Economics":
            case "商业管理":
                return "linear-gradient(145deg, #a8aca0, #676d60)";
            case "History":
                return "linear-gradient(145deg, #b7a17f, #76664f)";
            case "Science":
                return "linear-gradient(145deg, #a2afa7, #5d6b63)";
            case "Design":
                return "linear-gradient(145deg, #b4aa9d, #70675d)";
            case "Psychology":
                return "linear-gradient(145deg, #aaa79b, #68665e)";
            case "Philosophy":
                return "linear-gradient(145deg, #aaa18e, #695f4e)";
            case "Art":
                return "linear-gradient(145deg, #b7a090, #745e51)";
            case "Travel":
                return "linear-gradient(145deg, #9eaaa7, #5f6b68)";
            case "Education":
                return "linear-gradient(145deg, #a6ad98, #646b58)";
            case "Biography":
                return "linear-gradient(145deg, #b0a0aa, #665d68)";
            default:
                return "linear-gradient(145deg, #b0a79c, #655f58)";
        }
    }
}
