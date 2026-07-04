<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="订单管理 - Book Nest" scope="request"/>
<%@ include file="../common/header.jspf" %>

<section class="toolbar reveal">
    <div>
        <span class="eyebrow">Admin</span>
        <h1>订单管理</h1>
    </div>
    <a class="ghost-button" href="${pageContext.request.contextPath}/admin/books">图书管理</a>
</section>

<form class="admin-filter reveal" action="${pageContext.request.contextPath}/admin/orders" method="get">
    <input name="keyword" value="${keyword}" placeholder="搜索订单号或用户名">
    <select name="status">
        <option value="">全部订单</option>
        <option value="Paid" <c:if test="${selectedStatus == 'Paid'}">selected</c:if>>已支付，待发货</option>
        <option value="Shipped" <c:if test="${selectedStatus == 'Shipped'}">selected</c:if>>配送中</option>
        <option value="Completed" <c:if test="${selectedStatus == 'Completed'}">selected</c:if>>已完成</option>
        <option value="Cancelled" <c:if test="${selectedStatus == 'Cancelled'}">selected</c:if>>已取消</option>
    </select>
    <button class="primary-button compact" type="submit">筛选</button>
    <a class="ghost-button compact" href="${pageContext.request.contextPath}/admin/orders">重置</a>
</form>

<section class="order-list admin-orders">
    <c:forEach items="${orders}" var="order">
        <article class="order-card reveal">
            <div class="order-head">
                <div>
                    <span class="eyebrow">${order.orderNo} · ${order.username}</span>
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
            <form class="order-status-form" action="${pageContext.request.contextPath}/admin/orders" method="post">
                <input type="hidden" name="orderId" value="${order.orderId}">
                <select name="status">
                    <option value="Paid" <c:if test="${order.status == 'Paid'}">selected</c:if>>已支付，待发货</option>
                    <option value="Shipped" <c:if test="${order.status == 'Shipped'}">selected</c:if>>配送中</option>
                    <option value="Completed" <c:if test="${order.status == 'Completed'}">selected</c:if>>已完成</option>
                    <option value="Cancelled" <c:if test="${order.status == 'Cancelled'}">selected</c:if>>已取消</option>
                </select>
                <button class="ghost-button" type="submit">更新状态</button>
            </form>
        </article>
    </c:forEach>
</section>

<c:if test="${empty orders}">
    <section class="empty-state reveal">
        <h2>暂无订单</h2>
        <p>用户完成结算后，订单会出现在这里。</p>
    </section>
</c:if>

<%@ include file="../common/footer.jspf" %>
