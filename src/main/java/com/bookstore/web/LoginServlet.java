package com.bookstore.web;

import com.bookstore.dao.BookDao;
import com.bookstore.dao.CartDao;
import com.bookstore.dao.UserDao;
import com.bookstore.model.Book;
import com.bookstore.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private final UserDao userDao = new UserDao();
    private final CartDao cartDao = new CartDao();
    private final BookDao bookDao = new BookDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("currentPage", "login");
        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = trim(request.getParameter("username"));
        String password = request.getParameter("password");
        try {
            Optional<User> user = userDao.authenticate(username, password);
            if (user.isPresent()) {
                request.getSession().setAttribute("currentUser", user.get());
                mergeSessionCart(request, user.get());
                request.getSession().setAttribute("toast", "欢迎回来，" + user.get().getUsername());
                String target = user.get().isAdmin() ? "/admin/books" : "/books";
                response.sendRedirect(request.getContextPath() + target);
                return;
            }
            request.setAttribute("error", "用户名或密码不正确");
            request.setAttribute("username", username);
            request.setAttribute("currentPage", "login");
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException("Failed to login", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void mergeSessionCart(HttpServletRequest request, User user) throws SQLException {
        Object cart = request.getSession().getAttribute("cart");
        if (!(cart instanceof Map)) {
            return;
        }
        Map<Long, Integer> sessionCart = (Map<Long, Integer>) cart;
        for (Map.Entry<Long, Integer> entry : sessionCart.entrySet()) {
            Optional<Book> book = bookDao.findById(entry.getKey());
            if (book.isPresent()) {
                cartDao.add(user.getId(), entry.getKey(), entry.getValue(), book.get().getStock());
            }
        }
        sessionCart.clear();
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
