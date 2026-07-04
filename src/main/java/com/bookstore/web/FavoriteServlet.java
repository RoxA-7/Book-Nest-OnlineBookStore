package com.bookstore.web;

import com.bookstore.dao.BookDao;
import com.bookstore.dao.CartDao;
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
import java.util.Optional;

@WebServlet("/favorites")
public class FavoriteServlet extends HttpServlet {
    private final FavoriteDao favoriteDao = new FavoriteDao();
    private final BookDao bookDao = new BookDao();
    private final CartDao cartDao = new CartDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = requireLogin(request, response);
        if (user == null) {
            return;
        }
        try {
            request.setAttribute("currentPage", "favorites");
            request.setAttribute("favorites", favoriteDao.findByUser(user.getId()));
            request.getRequestDispatcher("/WEB-INF/views/favorites.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException("Failed to load favorites", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = requireLogin(request, response);
        if (user == null) {
            return;
        }
        String action = request.getParameter("action");
        long bookId = parseLong(request.getParameter("bookId"));
        try {
            if ("remove".equals(action)) {
                favoriteDao.remove(user.getId(), bookId);
                request.getSession().setAttribute("toast", "已从收藏夹移除");
            } else if ("addCart".equals(action)) {
                Optional<Book> book = bookDao.findById(bookId);
                if (book.isPresent()) {
                    cartDao.add(user.getId(), bookId, 1, book.get().getStock());
                    request.getSession().setAttribute("toast", "已加入书单");
                }
            } else {
                favoriteDao.add(user.getId(), bookId);
                request.getSession().setAttribute("toast", "已加入收藏夹");
            }
            response.sendRedirect(resolveReturnTo(request));
        } catch (SQLException e) {
            throw new ServletException("Failed to update favorites", e);
        }
    }

    private User requireLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = (User) request.getSession().getAttribute("currentUser");
        if (user == null) {
            request.getSession().setAttribute("toast", "请先登录后使用收藏夹");
            response.sendRedirect(request.getContextPath() + "/login");
        }
        return user;
    }

    private String resolveReturnTo(HttpServletRequest request) {
        String returnTo = request.getParameter("returnTo");
        if (returnTo != null && returnTo.startsWith(request.getContextPath())) {
            return returnTo;
        }
        return request.getContextPath() + "/favorites";
    }

    private long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
