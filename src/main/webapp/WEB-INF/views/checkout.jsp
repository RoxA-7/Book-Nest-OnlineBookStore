<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="确认结算 - Book Nest" scope="request"/>
<%@ include file="common/header.jspf" %>

<section class="toolbar reveal">
    <div>
        <span class="eyebrow">Checkout</span>
        <h1>确认结算</h1>
    </div>
    <a class="ghost-button" href="${pageContext.request.contextPath}/cart">返回书单</a>
</section>

<section class="checkout-layout">
    <form class="form-card checkout-form reveal" action="${pageContext.request.contextPath}/checkout" method="post">
        <c:if test="${not empty error}"><div class="form-error">${error}</div></c:if>
        <span class="eyebrow">Delivery</span>
        <h2>收货信息</h2>
        <label>收货地址<textarea name="address" rows="4" placeholder="省市区、街道、门牌号、收件人和手机号" required>${param.address}</textarea></label>
        <span class="eyebrow payment-label">Payment</span>
        <h2>支付方式</h2>
        <div class="payment-grid">
            <label><input type="radio" name="paymentMethod" value="Alipay" checked> 支付宝</label>
            <label><input type="radio" name="paymentMethod" value="WeChat Pay"> 微信支付</label>
            <label><input type="radio" name="paymentMethod" value="Bank Card"> 银行卡</label>
        </div>
        <button class="primary-button full" type="submit">提交订单</button>
    </form>

    <aside class="summary-card reveal">
        <span class="eyebrow">Order summary</span>
        <h2>订单摘要</h2>
        <c:forEach items="${cartSummary.items}" var="item">
            <div class="summary-row">
                <span>${item.book.title} × ${item.quantity}</span>
                <strong>￥<fmt:formatNumber value="${item.subtotal}" minFractionDigits="2"/></strong>
            </div>
        </c:forEach>
        <div class="summary-row total">
            <span>应付合计</span>
            <strong>￥<fmt:formatNumber value="${cartSummary.totalPrice}" minFractionDigits="2"/></strong>
        </div>
    </aside>
</section>

<%@ include file="common/footer.jspf" %>
