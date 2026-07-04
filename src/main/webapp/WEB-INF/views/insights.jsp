<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Reading Insights - Book Nest" scope="request"/>
<%@ include file="common/header.jspf" %>

<section class="toolbar reveal">
    <div>
        <span class="eyebrow">Statistics</span>
        <h1>Reading Insights</h1>
    </div>
    <a class="ghost-button" href="${pageContext.request.contextPath}/books">继续浏览</a>
</section>

<section class="insight-grid reveal">
    <article class="insight-hero">
        <span class="eyebrow">My browsing statistics</span>
        <h2>${userTotalViews}</h2>
        <p>total views across ${userViewedBooks} books</p>
    </article>
    <div class="metric-card"><strong>${userFavorites}</strong><span>收藏图书</span></div>
    <div class="metric-card"><strong>${userOrders}</strong><span>历史订单</span></div>
    <div class="metric-card"><strong>${siteViews}</strong><span>全站浏览</span></div>
</section>

<section class="insight-layout">
    <article class="analytics-panel reveal">
        <div class="panel-title">
            <span class="eyebrow">Categories</span>
            <h2>Books viewed by category</h2>
        </div>
        <c:forEach items="${categoryStats}" var="item">
            <div class="bar-row">
                <span>${item.label}</span>
                <div class="bar-track"><div style="width:${item.percent}%"></div></div>
                <strong>${item.value}</strong>
            </div>
        </c:forEach>
        <c:if test="${empty categoryStats}">
            <p class="muted-text">浏览几本书后，这里会显示你的分类偏好。</p>
        </c:if>
    </article>

    <article class="analytics-panel reveal">
        <div class="panel-title">
            <span class="eyebrow">History</span>
            <h2>最近浏览记录</h2>
        </div>
        <div class="history-list">
            <c:forEach items="${recentHistory}" var="record">
                <a class="history-line" href="${pageContext.request.contextPath}/book?id=${record.book.id}">
                    <span>${record.book.title}</span>
                    <small>${record.lastViewedText} · ${record.viewCount} 次</small>
                </a>
            </c:forEach>
        </div>
        <c:if test="${empty recentHistory}">
            <p class="muted-text">暂时没有浏览历史。</p>
        </c:if>
    </article>
</section>

<section class="insight-layout">
    <article class="analytics-panel reveal">
        <div class="panel-title">
            <span class="eyebrow">Site</span>
            <h2>网站数据</h2>
        </div>
        <div class="stat-table">
            <div><span>在架图书</span><strong>${siteBooks}</strong></div>
            <div><span>分类数量</span><strong>${siteCategories}</strong></div>
            <div><span>订单数量</span><strong>${siteOrders}</strong></div>
            <div><span>收藏总数</span><strong>${siteFavorites}</strong></div>
            <div><span>销售额</span><strong>￥<fmt:formatNumber value="${siteRevenue}" minFractionDigits="2"/></strong></div>
        </div>
    </article>

    <article class="analytics-panel reveal">
        <div class="panel-title">
            <span class="eyebrow">Popular</span>
            <h2>热门图书</h2>
        </div>
        <c:forEach items="${topViewedBooks}" var="item">
            <div class="bar-row">
                <span>${item.label}</span>
                <div class="bar-track"><div style="width:${item.percent}%"></div></div>
                <strong>${item.value}</strong>
            </div>
        </c:forEach>
        <c:if test="${empty topViewedBooks}">
            <p class="muted-text">暂无全站浏览统计。</p>
        </c:if>
    </article>
</section>

<section class="analytics-panel reveal">
    <div class="panel-title">
        <span class="eyebrow">Saved</span>
        <h2>收藏最多的书</h2>
    </div>
    <c:forEach items="${topFavoritedBooks}" var="item">
        <div class="bar-row">
            <span>${item.label}</span>
            <div class="bar-track"><div style="width:${item.percent}%"></div></div>
            <strong>${item.value}</strong>
        </div>
    </c:forEach>
    <c:if test="${empty topFavoritedBooks}">
        <p class="muted-text">还没有收藏数据。</p>
    </c:if>
</section>

<%@ include file="common/footer.jspf" %>
