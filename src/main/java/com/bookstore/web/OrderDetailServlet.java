package com.bookstore.web;

import com.bookstore.dao.OrderDao;
import com.bookstore.model.Order;
import com.bookstore.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/order")
public class OrderDetailServlet extends HttpServlet {
    private final OrderDao orderDao = new OrderDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("currentUser");
        if (user == null) {
            request.getSession().setAttribute("toast", "请先登录后查看订单详情");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        long orderId = parseLong(request.getParameter("id"));
        try {
            Order order = user.isAdmin() ? orderDao.findById(orderId) : orderDao.findByIdForUser(orderId, user.getId());
            if (order == null) {
                request.getSession().setAttribute("toast", "没有找到这笔订单");
                response.sendRedirect(request.getContextPath() + "/orders");
                return;
            }
            request.setAttribute("currentPage", "orders");
            request.setAttribute("order", order);
            request.getRequestDispatcher("/WEB-INF/views/order-detail.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException("Failed to load order detail", e);
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
