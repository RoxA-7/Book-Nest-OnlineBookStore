<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="我的订单 - Book Nest" scope="request"/>
<%@ include file="common/header.jspf" %>

<section class="toolbar reveal">
    <div>
        <span class="eyebrow">Orders</span>
        <h1>我的订单</h1>
    </div>
    <a class="ghost-button" href="${pageContext.request.contextPath}/books">继续选书</a>
</section>

<c:choose>
    <c:when test="${empty orders}">
        <section class="empty-state reveal">
            <h2>还没有历史订单</h2>
            <p>完成一次结算后，你可以在这里查看订单状态和明细。</p>
            <a class="primary-button compact" href="${pageContext.request.contextPath}/books">去选书</a>
        </section>
    </c:when>
    <c:otherwise>
        <section class="order-list">
            <c:forEach items="${orders}" var="order">
                <article class="order-card reveal">
                    <div class="order-head">
                        <div>
                            <span class="eyebrow">${order.orderNo}</span>
                            <h2>${order.statusText}</h2>
                            <p>${order.address}</p>
                            <p class="muted-text">下单时间 ${order.createdAtText}</p>
                        </div>
                        <div class="order-total">
                            <span>${order.paymentMethod}</span>
                            <strong>￥<fmt:formatNumber value="${order.totalPrice}" minFractionDigits="2"/></strong>
                        </div>
                    </div>
                    <div class="order-items">
                        <c:forEach items="${order.items}" var="item">
                            <div class="order-item-line">
                                <span>${item.book.title}</span>
                                <span>× ${item.quantity}</span>
                                <strong>￥<fmt:formatNumber value="${item.subtotal}" minFractionDigits="2"/></strong>
                            </div>
                        </c:forEach>
                    </div>
                    <form class="order-actions" action="${pageContext.request.contextPath}/orders" method="post">
                        <input type="hidden" name="orderId" value="${order.orderId}">
                        <button class="ghost-button compact" type="submit">再次购买</button>
                    </form>
                </article>
            </c:forEach>
        </section>
    </c:otherwise>
</c:choose>

<%@ include file="common/footer.jspf" %>
