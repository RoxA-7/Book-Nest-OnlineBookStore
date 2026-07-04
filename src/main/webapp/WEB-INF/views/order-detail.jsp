<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="订单详情 - Book Nest" scope="request"/>
<%@ include file="common/header.jspf" %>

<section class="toolbar reveal">
    <div>
        <span class="eyebrow">${order.orderNo}</span>
        <h1>订单详情</h1>
    </div>
    <a class="ghost-button" href="${pageContext.request.contextPath}/orders">返回订单</a>
</section>

<section class="order-detail-layout">
    <article class="order-card reveal">
        <div class="order-head">
            <div>
                <span class="eyebrow">${order.statusText}</span>
                <h2>${order.orderNo}</h2>
                <p>${order.address}</p>
                <p class="muted-text">下单时间 ${order.createdAtText}</p>
            </div>
            <div class="order-total">
                <span>${order.paymentMethod}</span>
                <strong>￥<fmt:formatNumber value="${order.totalPrice}" minFractionDigits="2"/></strong>
            </div>
        </div>
        <div class="progress-steps ${order.status == 'Cancelled' ? 'cancelled' : ''}">
            <span class="active">已支付</span>
            <span class="${order.status == 'Shipped' || order.status == 'Completed' ? 'active' : ''}">配送中</span>
            <span class="${order.status == 'Completed' ? 'active' : ''}">已完成</span>
        </div>
        <div class="order-items">
            <c:forEach items="${order.items}" var="item">
                <div class="order-item-line">
                    <a href="${pageContext.request.contextPath}/book?id=${item.book.id}">${item.book.title}</a>
                    <span>× ${item.quantity}</span>
                    <strong>￥<fmt:formatNumber value="${item.subtotal}" minFractionDigits="2"/></strong>
                </div>
            </c:forEach>
        </div>
        <form class="order-actions" action="${pageContext.request.contextPath}/orders" method="post">
            <input type="hidden" name="orderId" value="${order.orderId}">
            <button class="primary-button compact" type="submit">再次购买</button>
        </form>
    </article>
</section>

<%@ include file="common/footer.jspf" %>
