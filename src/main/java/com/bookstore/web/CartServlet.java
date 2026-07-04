package com.bookstore.web;

import com.bookstore.dao.BookDao;
import com.bookstore.model.Book;
import com.bookstore.model.CartItem;
import com.bookstore.model.CartSummary;

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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("cartSummary", buildSummary(getCart(request)));
            request.getRequestDispatcher("/WEB-INF/views/cart.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException("Failed to load cart", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        Map<Long, Integer> cart = getCart(request);
        try {
            if ("add".equals(action)) {
                addBook(request, cart);
                request.getSession().setAttribute("toast", "已加入书单");
                response.sendRedirect(safeRedirect(request));
                return;
            }
            if ("update".equals(action)) {
                long bookId = parseLong(request.getParameter("bookId"));
                int quantity = Math.max(1, parseInt(request.getParameter("quantity")));
                Optional<Book> book = bookDao.findById(bookId);
                book.ifPresent(value -> cart.put(bookId, Math.min(quantity, value.getStock())));
                request.getSession().setAttribute("toast", "书单数量已更新");
            } else if ("remove".equals(action)) {
                cart.remove(parseLong(request.getParameter("bookId")));
                request.getSession().setAttribute("toast", "已移除图书");
            } else if ("clear".equals(action)) {
                cart.clear();
                request.getSession().setAttribute("toast", "书单已清空");
            }
            response.sendRedirect(request.getContextPath() + "/cart");
        } catch (SQLException e) {
            throw new ServletException("Failed to update cart", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<Long, Integer> getCart(HttpServletRequest request) {
        Object cart = request.getSession().getAttribute("cart");
        if (cart instanceof Map) {
            return (Map<Long, Integer>) cart;
        }
        Map<Long, Integer> newCart = new HashMap<>();
        request.getSession().setAttribute("cart", newCart);
        return newCart;
    }

    private void addBook(HttpServletRequest request, Map<Long, Integer> cart) throws SQLException {
        long bookId = parseLong(request.getParameter("bookId"));
        Optional<Book> book = bookDao.findById(bookId);
        if (book.isEmpty() || book.get().getStock() <= 0) {
            return;
        }
        int current = cart.getOrDefault(bookId, 0);
        cart.put(bookId, Math.min(current + 1, book.get().getStock()));
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
            CartItem item = new CartItem(book.get(), quantity);
            items.add(item);
            totalQuantity += quantity;
            totalPrice = totalPrice.add(item.getSubtotal());
        }
        return new CartSummary(items, totalQuantity, totalPrice);
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

