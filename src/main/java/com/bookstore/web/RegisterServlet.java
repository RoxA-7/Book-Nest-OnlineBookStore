package com.bookstore.web;

import com.bookstore.dao.UserDao;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("currentPage", "register");
        request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = trim(request.getParameter("username"));
        String email = trim(request.getParameter("email"));
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        if (username.length() < 3 || password == null || password.length() < 6 || !password.equals(confirmPassword)) {
            request.setAttribute("error", "请填写至少 3 位用户名、6 位密码，并确认两次密码一致");
            request.setAttribute("username", username);
            request.setAttribute("email", email);
            request.setAttribute("currentPage", "register");
            request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
            return;
        }
        try {
            if (userDao.findByUsername(username).isPresent()) {
                request.setAttribute("error", "用户名已存在，请换一个更特别的名字");
                request.setAttribute("username", username);
                request.setAttribute("email", email);
                request.setAttribute("currentPage", "register");
                request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
                return;
            }
            userDao.create(username, password, email);
            request.getSession().setAttribute("toast", "注册成功，请登录");
            response.sendRedirect(request.getContextPath() + "/login");
        } catch (SQLException e) {
            throw new ServletException("Failed to register user", e);
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
