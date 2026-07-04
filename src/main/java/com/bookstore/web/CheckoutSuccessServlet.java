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

@WebServlet("/checkout/success")
public class CheckoutSuccessServlet extends HttpServlet {
    private final OrderDao orderDao = new OrderDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("currentUser");
        if (user == null) {
            request.getSession().setAttribute("toast", "请先登录后查看支付结果");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        try {
            Order order = orderDao.findByIdForUser(parseLong(request.getParameter("orderId")), user.getId());
            if (order == null) {
                request.getSession().setAttribute("toast", "没有找到这笔订单");
                response.sendRedirect(request.getContextPath() + "/orders");
                return;
            }
            request.setAttribute("currentPage", "orders");
            request.setAttribute("order", order);
            request.getRequestDispatcher("/WEB-INF/views/checkout-success.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException("Failed to load checkout success", e);
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
