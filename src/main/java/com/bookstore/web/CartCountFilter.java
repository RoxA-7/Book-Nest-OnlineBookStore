package com.bookstore.web;

import com.bookstore.dao.CartDao;
import com.bookstore.model.User;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

@WebFilter("/*")
public class CartCountFilter implements Filter {
    private final CartDao cartDao = new CartDao();

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        if (!isStaticAsset(httpRequest)) {
            request.setAttribute("cartCount", resolveCartCount(httpRequest));
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

    @SuppressWarnings("unchecked")
    private int resolveCartCount(HttpServletRequest request) throws ServletException {
        User user = (User) request.getSession().getAttribute("currentUser");
        if (user != null) {
            try {
                return cartDao.countItems(user.getId());
            } catch (SQLException e) {
                throw new ServletException("Failed to count cart items", e);
            }
        }
        Object cart = request.getSession().getAttribute("cart");
        if (cart instanceof Map) {
            return ((Map<Long, Integer>) cart).values().stream().mapToInt(Integer::intValue).sum();
        }
        return 0;
    }

    private boolean isStaticAsset(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.contains("/assets/");
    }
}
