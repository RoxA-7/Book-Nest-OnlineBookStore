package com.bookstore.web;

import com.bookstore.dao.OrderDao;
import com.bookstore.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/orders")
public class OrdersServlet extends HttpServlet {
    private final OrderDao orderDao = new OrderDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("currentUser");
        if (user == null) {
            request.getSession().setAttribute("toast", "请先登录后查看订单");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        try {
            request.setAttribute("currentPage", "orders");
            request.setAttribute("orders", orderDao.findByUser(user.getId()));
            request.getRequestDispatcher("/WEB-INF/views/orders.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException("Failed to load orders", e);
        }
    }
}
