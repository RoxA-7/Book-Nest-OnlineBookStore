package com.bookstore.web;

import com.bookstore.dao.CartDao;
import com.bookstore.dao.OrderDao;
import com.bookstore.model.CartSummary;
import com.bookstore.model.Order;
import com.bookstore.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/checkout")
public class CheckoutServlet extends HttpServlet {
    private final CartDao cartDao = new CartDao();
    private final OrderDao orderDao = new OrderDao();
    private final CartServlet cartServlet = new CartServlet();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = requireLogin(request, response);
        if (user == null) {
            return;
        }
        try {
            CartSummary summary = cartServlet.buildSummary(cartDao.findCart(user.getId()));
            if (summary.getItems().isEmpty()) {
                request.getSession().setAttribute("toast", "书单为空，暂时不能结算");
                response.sendRedirect(request.getContextPath() + "/cart");
                return;
            }
            request.setAttribute("currentPage", "checkout");
            request.setAttribute("cartSummary", summary);
            request.getRequestDispatcher("/WEB-INF/views/checkout.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException("Failed to load checkout", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = requireLogin(request, response);
        if (user == null) {
            return;
        }
        String address = trim(request.getParameter("address"));
        String paymentMethod = trim(request.getParameter("paymentMethod"));
        if (address.length() < 8 || paymentMethod.isEmpty()) {
            request.setAttribute("error", "请填写完整收货地址，并选择支付方式");
            doGet(request, response);
            return;
        }
        try {
            CartSummary summary = cartServlet.buildSummary(cartDao.findCart(user.getId()));
            if (summary.getItems().isEmpty()) {
                request.getSession().setAttribute("toast", "书单为空，暂时不能结算");
                response.sendRedirect(request.getContextPath() + "/cart");
                return;
            }
            Order order = orderDao.createOrder(user.getId(), summary, address, paymentMethod);
            request.getSession().setAttribute("orderSuccessNo", order.getOrderNo());
            request.getSession().setAttribute("orderSuccessTotal", order.getTotalPrice());
            request.getSession().setAttribute("toast", "订单已提交，订单号 " + order.getOrderNo());
            response.sendRedirect(request.getContextPath() + "/checkout/success?orderId=" + order.getOrderId());
        } catch (SQLException e) {
            throw new ServletException("Failed to create order", e);
        }
    }

    private User requireLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = (User) request.getSession().getAttribute("currentUser");
        if (user == null) {
            request.getSession().setAttribute("toast", "请先登录后再结算");
            response.sendRedirect(request.getContextPath() + "/login");
        }
        return user;
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
