package com.bookstore.web;

import com.bookstore.dao.OrderDao;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/admin/orders")
public class AdminOrderServlet extends HttpServlet {
    private final OrderDao orderDao = new OrderDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String status = trim(request.getParameter("status"));
        try {
            request.setAttribute("currentPage", "adminOrders");
            request.setAttribute("selectedStatus", status);
            request.setAttribute("orders", orderDao.findAll(status));
            request.getRequestDispatcher("/WEB-INF/views/admin/orders.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException("Failed to load admin orders", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            orderDao.updateStatus(parseLong(request.getParameter("orderId")), request.getParameter("status"));
            request.getSession().setAttribute("toast", "订单状态已更新");
            response.sendRedirect(request.getContextPath() + "/admin/orders");
        } catch (SQLException e) {
            throw new ServletException("Failed to update order", e);
        }
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
