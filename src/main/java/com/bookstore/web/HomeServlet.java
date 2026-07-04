package com.bookstore.web;

import com.bookstore.dao.BookDao;
import com.bookstore.dao.BrowsingHistoryDao;
import com.bookstore.dao.FavoriteDao;
import com.bookstore.model.Book;
import com.bookstore.model.User;
import com.bookstore.util.HighlightUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/books")
public class HomeServlet extends HttpServlet {
    private final BookDao bookDao = new BookDao();
    private final FavoriteDao favoriteDao = new FavoriteDao();
    private final BrowsingHistoryDao historyDao = new BrowsingHistoryDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String category = request.getParameter("category");
        String keyword = request.getParameter("keyword");
        try {
            List<Book> books = bookDao.findBooks(category, keyword);
            applyHighlights(books, keyword);
            User user = (User) request.getSession().getAttribute("currentUser");
            if (user != null) {
                historyDao.recordViews(user.getId(), books);
                request.setAttribute("favoriteBookIds", favoriteDao.findFavoriteBookIds(user.getId()));
                request.setAttribute("recentHistory", historyDao.findRecentByUser(user.getId(), 3));
            }
            request.setAttribute("currentPage", "books");
            request.setAttribute("books", books);
            request.setAttribute("featuredBooks", bookDao.findFeatured());
            request.setAttribute("categories", bookDao.findCategories());
            request.setAttribute("bookCount", bookDao.countAll());
            request.setAttribute("selectedCategory", category == null ? "" : category);
            request.setAttribute("keyword", keyword == null ? "" : keyword);
            request.setAttribute("headerKeyword", keyword == null ? "" : keyword);
            request.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException("Failed to load books", e);
        }
    }

    private void applyHighlights(List<Book> books, String keyword) {
        for (Book book : books) {
            book.setHighlightedTitle(HighlightUtil.highlight(book.getTitle(), keyword));
            book.setHighlightedAuthor(HighlightUtil.highlight(book.getAuthor(), keyword));
            book.setHighlightedDescription(HighlightUtil.highlight(book.getDescription(), keyword));
        }
    }
}

