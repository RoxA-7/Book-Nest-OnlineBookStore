package com.bookstore.web;

import com.bookstore.dao.BookDao;
import com.bookstore.dao.BrowsingHistoryDao;
import com.bookstore.dao.FavoriteDao;
import com.bookstore.dao.OrderDao;
import com.bookstore.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/insights")
public class InsightsServlet extends HttpServlet {
    private final BookDao bookDao = new BookDao();
    private final BrowsingHistoryDao historyDao = new BrowsingHistoryDao();
    private final FavoriteDao favoriteDao = new FavoriteDao();
    private final OrderDao orderDao = new OrderDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("currentUser");
        if (user == null) {
            request.getSession().setAttribute("toast", "请先登录后查看阅读统计");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        try {
            request.setAttribute("currentPage", "insights");
            request.setAttribute("userViewedBooks", historyDao.countBooksByUser(user.getId()));
            request.setAttribute("userTotalViews", historyDao.countViewsByUser(user.getId()));
            request.setAttribute("userFavorites", favoriteDao.countByUser(user.getId()));
            request.setAttribute("userOrders", orderDao.countByUser(user.getId()));
            request.setAttribute("recentHistory", historyDao.findRecentByUser(user.getId(), 12));
            request.setAttribute("categoryStats", historyDao.categoryStatsByUser(user.getId()));

            request.setAttribute("siteBooks", bookDao.countAll());
            request.setAttribute("siteCategories", bookDao.countCategories());
            request.setAttribute("siteOrders", orderDao.countAll());
            request.setAttribute("siteRevenue", orderDao.totalRevenue());
            request.setAttribute("siteFavorites", favoriteDao.countAll());
            request.setAttribute("siteViews", historyDao.countAllViews());
            request.setAttribute("topViewedBooks", historyDao.topViewedBooks(5));
            request.setAttribute("topFavoritedBooks", favoriteDao.topFavoritedBooks(5));

            request.getRequestDispatcher("/WEB-INF/views/insights.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException("Failed to load insights", e);
        }
    }
}
