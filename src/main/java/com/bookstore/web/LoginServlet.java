package com.bookstore.web;

import com.bookstore.dao.UserDao;
import com.bookstore.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
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
                request.getSession().setAttribute("toast", "欢迎回来，" + user.get().getUsername());
                String target = user.get().isAdmin() ? "/admin/books" : "/books";
                response.sendRedirect(request.getContextPath() + target);
                return;
            }
            request.setAttribute("error", "用户名或密码不正确");
            request.setAttribute("username", username);
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException("Failed to login", e);
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}

