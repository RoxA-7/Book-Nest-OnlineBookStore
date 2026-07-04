package com.bookstore.web;

import com.bookstore.dao.BookDao;
import com.bookstore.dao.CartDao;
import com.bookstore.dao.OrderDao;
import com.bookstore.model.Book;
import com.bookstore.model.CartItem;
import com.bookstore.model.CartSummary;
import com.bookstore.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@WebServlet("/cart")
public class CartServlet extends HttpServlet {
    private final BookDao bookDao = new BookDao();
    private final CartDao cartDao = new CartDao();
    private final OrderDao orderDao = new OrderDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("currentPage", "cart");
        try {
            request.setAttribute("cartSummary", buildSummary(resolveCart(request)));
            request.getRequestDispatcher("/WEB-INF/views/cart.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException("Failed to load cart", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        try {
            if ("add".equals(action)) {
                addBook(request);
                request.getSession().setAttribute("toast", "已加入书单");
                response.sendRedirect(safeRedirect(request));
                return;
            }
            if ("checkout".equals(action)) {
                checkout(request, response);
                return;
            }
            if ("update".equals(action)) {
                updateBook(request);
                request.getSession().setAttribute("toast", "书单数量已更新");
            } else if ("remove".equals(action)) {
                removeBook(request);
                request.getSession().setAttribute("toast", "已移除图书");
            } else if ("clear".equals(action)) {
                clearCart(request);
                request.getSession().setAttribute("toast", "书单已清空");
            }
            response.sendRedirect(request.getContextPath() + "/cart");
        } catch (SQLException e) {
            throw new ServletException("Failed to update cart", e);
        }
    }

    private void addBook(HttpServletRequest request) throws SQLException {
        long bookId = parseLong(request.getParameter("bookId"));
        Optional<Book> book = bookDao.findById(bookId);
        if (book.isEmpty() || book.get().getStock() <= 0) {
            return;
        }
        User user = currentUser(request);
        if (user != null) {
            cartDao.add(user.getId(), bookId, 1, book.get().getStock());
            return;
        }
        Map<Long, Integer> cart = sessionCart(request);
        int current = cart.getOrDefault(bookId, 0);
        cart.put(bookId, Math.min(current + 1, book.get().getStock()));
    }

    private void updateBook(HttpServletRequest request) throws SQLException {
        long bookId = parseLong(request.getParameter("bookId"));
        int quantity = Math.max(1, parseInt(request.getParameter("quantity")));
        Optional<Book> book = bookDao.findById(bookId);
        if (book.isEmpty()) {
            return;
        }
        int safeQuantity = Math.min(quantity, book.get().getStock());
        User user = currentUser(request);
        if (user != null) {
            cartDao.update(user.getId(), bookId, safeQuantity);
        } else {
            sessionCart(request).put(bookId, safeQuantity);
        }
    }

    private void removeBook(HttpServletRequest request) throws SQLException {
        long bookId = parseLong(request.getParameter("bookId"));
        User user = currentUser(request);
        if (user != null) {
            cartDao.remove(user.getId(), bookId);
        } else {
            sessionCart(request).remove(bookId);
        }
    }

    private void clearCart(HttpServletRequest request) throws SQLException {
        User user = currentUser(request);
        if (user != null) {
            cartDao.clear(user.getId());
        } else {
            sessionCart(request).clear();
        }
    }

    private void checkout(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        User user = currentUser(request);
        if (user == null) {
            request.getSession().setAttribute("toast", "请先登录后再结算");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        CartSummary summary = buildSummary(cartDao.findCart(user.getId()));
        if (summary.getItems().isEmpty()) {
            request.getSession().setAttribute("toast", "书单为空，暂时不能结算");
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }
        long orderId = orderDao.createOrder(user.getId(), summary);
        request.getSession().setAttribute("toast", "结算成功，订单号 #" + orderId);
        response.sendRedirect(request.getContextPath() + "/cart");
    }

    private Map<Long, Integer> resolveCart(HttpServletRequest request) throws SQLException {
        User user = currentUser(request);
        return user == null ? sessionCart(request) : cartDao.findCart(user.getId());
    }

    @SuppressWarnings("unchecked")
    private Map<Long, Integer> sessionCart(HttpServletRequest request) {
        Object cart = request.getSession().getAttribute("cart");
        if (cart instanceof Map) {
            return (Map<Long, Integer>) cart;
        }
        Map<Long, Integer> newCart = new HashMap<>();
        request.getSession().setAttribute("cart", newCart);
        return newCart;
    }

    private CartSummary buildSummary(Map<Long, Integer> cart) throws SQLException {
        List<CartItem> items = new ArrayList<>();
        int totalQuantity = 0;
        BigDecimal totalPrice = BigDecimal.ZERO;
        Iterator<Map.Entry<Long, Integer>> iterator = cart.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, Integer> entry = iterator.next();
            Optional<Book> book = bookDao.findById(entry.getKey());
            if (book.isEmpty()) {
                iterator.remove();
                continue;
            }
            int quantity = Math.min(entry.getValue(), book.get().getStock());
            if (quantity <= 0) {
                continue;
            }
            CartItem item = new CartItem(book.get(), quantity);
            items.add(item);
            totalQuantity += quantity;
            totalPrice = totalPrice.add(item.getSubtotal());
        }
        return new CartSummary(items, totalQuantity, totalPrice);
    }

    private User currentUser(HttpServletRequest request) {
        return (User) request.getSession().getAttribute("currentUser");
    }

    private String safeRedirect(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        String contextPath = request.getContextPath();
        if (referer != null && referer.contains(contextPath)) {
            return referer;
        }
        return contextPath + "/books";
    }

    private long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}
