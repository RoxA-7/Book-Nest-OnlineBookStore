package com.bookstore.web;

import com.bookstore.dao.BookDao;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/books")
public class HomeServlet extends HttpServlet {
    private final BookDao bookDao = new BookDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String category = request.getParameter("category");
        String keyword = request.getParameter("keyword");
        try {
            request.setAttribute("books", bookDao.findBooks(category, keyword));
            request.setAttribute("featuredBooks", bookDao.findFeatured());
            request.setAttribute("categories", bookDao.findCategories());
            request.setAttribute("bookCount", bookDao.countAll());
            request.setAttribute("selectedCategory", category == null ? "" : category);
            request.setAttribute("keyword", keyword == null ? "" : keyword);
            request.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException("Failed to load books", e);
        }
    }
}

