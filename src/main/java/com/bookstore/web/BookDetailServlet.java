package com.bookstore.web;

import com.bookstore.dao.BookDao;
import com.bookstore.dao.BrowsingHistoryDao;
import com.bookstore.dao.FavoriteDao;
import com.bookstore.model.Book;
import com.bookstore.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@WebServlet("/book")
public class BookDetailServlet extends HttpServlet {
    private final BookDao bookDao = new BookDao();
    private final FavoriteDao favoriteDao = new FavoriteDao();
    private final BrowsingHistoryDao historyDao = new BrowsingHistoryDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long bookId = parseLong(request.getParameter("id"));
        try {
            Optional<Book> book = bookDao.findById(bookId);
            if (book.isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/books");
                return;
            }
            User user = (User) request.getSession().getAttribute("currentUser");
            if (user != null) {
                historyDao.recordViews(user.getId(), List.of(book.get()));
                request.setAttribute("favoriteBookIds", favoriteDao.findFavoriteBookIds(user.getId()));
            }
            request.setAttribute("currentPage", "books");
            request.setAttribute("book", book.get());
            request.setAttribute("relatedBooks", bookDao.findRelated(book.get().getCategory(), book.get().getId(), 4));
            request.getRequestDispatcher("/WEB-INF/views/book-detail.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException("Failed to load book detail", e);
        }
    }

    private long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
