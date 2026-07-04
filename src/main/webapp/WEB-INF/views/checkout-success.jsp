<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="支付成功 - Book Nest" scope="request"/>
<%@ include file="common/header.jspf" %>

<section class="success-page reveal">
    <span class="eyebrow">Payment complete</span>
    <h1>支付成功</h1>
    <p>订单 ${order.orderNo} 已提交，后续状态会同步到你的订单记录。</p>
    <div class="success-total">￥<fmt:formatNumber value="${order.totalPrice}" minFractionDigits="2"/></div>
    <div class="progress-steps ${order.status == 'Cancelled' ? 'cancelled' : ''}">
        <span class="active">已支付</span>
        <span class="${order.status == 'Shipped' || order.status == 'Completed' ? 'active' : ''}">配送中</span>
        <span class="${order.status == 'Completed' ? 'active' : ''}">已完成</span>
    </div>
    <div class="modal-actions">
        <a class="primary-button" href="${pageContext.request.contextPath}/order?id=${order.orderId}">查看订单详情</a>
        <a class="ghost-button" href="${pageContext.request.contextPath}/books">继续选书</a>
    </div>
</section>

<%@ include file="common/footer.jspf" %>
