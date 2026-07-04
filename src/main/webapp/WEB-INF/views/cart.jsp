<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="我的书单 - Book Nest" scope="request"/>
<%@ include file="common/header.jspf" %>

<section class="toolbar reveal">
    <div>
        <span class="eyebrow">Reading list</span>
        <h1>我的书单</h1>
    </div>
    <a class="ghost-button" href="${pageContext.request.contextPath}/books">继续选书</a>
</section>

<c:choose>
    <c:when test="${empty cartSummary.items}">
        <section class="empty-state reveal">
            <h2>书单还是空的</h2>
            <p>从图书目录中挑几本书加入书单，这里会自动统计数量和金额。</p>
            <a class="primary-button compact" href="${pageContext.request.contextPath}/books">去选书</a>
        </section>
    </c:when>
    <c:otherwise>
        <section class="cart-layout">
            <div class="cart-list">
                <c:forEach items="${cartSummary.items}" var="item">
                    <article class="cart-item reveal">
                        <div class="mini-cover" style="background:${item.book.coverColor}">
                            <span>${fn:substring(item.book.title, 0, 1)}</span>
                        </div>
                        <div class="cart-main">
                            <h3>${item.book.title}</h3>
                            <p>${item.book.author} · ${item.book.category}</p>
                            <strong>￥<fmt:formatNumber value="${item.book.price}" minFractionDigits="2"/></strong>
                        </div>
                        <form class="quantity-form" action="${pageContext.request.contextPath}/cart" method="post">
                            <input type="hidden" name="action" value="update">
                            <input type="hidden" name="bookId" value="${item.book.id}">
                            <input name="quantity" type="number" min="1" max="${item.book.stock}" value="${item.quantity}">
                            <button class="ghost-button" type="submit">更新</button>
                        </form>
                        <form action="${pageContext.request.contextPath}/cart" method="post">
                            <input type="hidden" name="action" value="remove">
                            <input type="hidden" name="bookId" value="${item.book.id}">
                            <button class="text-button" type="submit">移除</button>
                        </form>
                    </article>
                </c:forEach>
            </div>
            <aside class="summary-card reveal">
                <span class="eyebrow">Summary</span>
                <h2>选书统计</h2>
                <div class="summary-row"><span>图书种类</span><strong>${cartSummary.items.size()}</strong></div>
                <div class="summary-row"><span>总数量</span><strong>${cartSummary.totalQuantity}</strong></div>
                <div class="summary-row total"><span>合计</span><strong>￥<fmt:formatNumber value="${cartSummary.totalPrice}" minFractionDigits="2"/></strong></div>
                <form action="${pageContext.request.contextPath}/cart" method="post">
                    <input type="hidden" name="action" value="clear">
                    <button class="ghost-button full" type="submit">清空书单</button>
                </form>
            </aside>
        </section>
    </c:otherwise>
</c:choose>

<%@ include file="common/footer.jspf" %>
