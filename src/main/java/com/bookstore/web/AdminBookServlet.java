package com.bookstore.web;

import com.bookstore.dao.BookDao;
import com.bookstore.model.Book;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;

@WebServlet("/admin/books")
public class AdminBookServlet extends HttpServlet {
    private final BookDao bookDao = new BookDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String category = trim(request.getParameter("category"));
        String keyword = trim(request.getParameter("keyword"));
        try {
            request.setAttribute("currentPage", "admin");
            request.setAttribute("books", bookDao.findBooks(category, keyword));
            request.setAttribute("categories", bookDao.findCategories());
            request.setAttribute("bookCount", bookDao.countAll());
            request.setAttribute("lowStockCount", bookDao.countLowStock());
            request.setAttribute("selectedCategory", category);
            request.setAttribute("keyword", keyword);
            request.setAttribute("headerKeyword", keyword);
            request.getRequestDispatcher("/WEB-INF/views/admin/books.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException("Failed to load admin books", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        try {
            if ("delete".equals(action)) {
                bookDao.delete(parseLong(request.getParameter("id")));
                request.getSession().setAttribute("toast", "图书已删除");
            } else if ("createCategory".equals(action)) {
                bookDao.createCategory(required(request.getParameter("categoryName")), trim(request.getParameter("categoryDescription")));
                request.getSession().setAttribute("toast", "分类已添加");
            } else if ("update".equals(action)) {
                Book book = readBook(request);
                book.setId(parseLong(request.getParameter("id")));
                bookDao.update(book);
                request.getSession().setAttribute("toast", "图书信息已更新");
            } else {
                bookDao.create(readBook(request));
                request.getSession().setAttribute("toast", "新图书已上架");
            }
            response.sendRedirect(request.getContextPath() + "/admin/books");
        } catch (SQLException | IllegalArgumentException e) {
            request.setAttribute("error", "保存失败，请检查书名、价格和库存等字段");
            doGet(request, response);
        }
    }

    private Book readBook(HttpServletRequest request) {
        Book book = new Book();
        book.setTitle(required(request.getParameter("title")));
        book.setAuthor(required(request.getParameter("author")));
        book.setCategory(required(request.getParameter("category")));
        book.setPrice(new BigDecimal(required(request.getParameter("price"))));
        book.setStock(Integer.parseInt(required(request.getParameter("stock"))));
        book.setDescription(required(request.getParameter("description")));
        book.setCoverColor(request.getParameter("coverColor"));
        book.setFeatured("on".equals(request.getParameter("featured")));
        return book;
    }

    private String required(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Required field is missing");
        }
        return value.trim();
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
