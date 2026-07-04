package com.bookstore.web;

import com.bookstore.dao.BookDao;
import com.bookstore.dao.CartDao;
import com.bookstore.dao.OrderDao;
import com.bookstore.model.Book;
import com.bookstore.model.Order;
import com.bookstore.model.OrderItem;
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
    private final CartDao cartDao = new CartDao();
    private final BookDao bookDao = new BookDao();

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

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("currentUser");
        if (user == null) {
            request.getSession().setAttribute("toast", "请先登录后操作订单");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        long orderId = parseLong(request.getParameter("orderId"));
        try {
            for (Order order : orderDao.findByUser(user.getId())) {
                if (order.getOrderId() == orderId) {
                    for (OrderItem item : order.getItems()) {
                        Book book = bookDao.findById(item.getBook().getId()).orElse(null);
                        if (book != null && book.getStock() > 0) {
                            cartDao.add(user.getId(), book.getId(), item.getQuantity(), book.getStock());
                        }
                    }
                    request.getSession().setAttribute("toast", "订单内图书已重新加入书单");
                    response.sendRedirect(request.getContextPath() + "/cart");
                    return;
                }
            }
            request.getSession().setAttribute("toast", "没有找到这笔订单");
            response.sendRedirect(request.getContextPath() + "/orders");
        } catch (SQLException e) {
            throw new ServletException("Failed to reorder", e);
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
