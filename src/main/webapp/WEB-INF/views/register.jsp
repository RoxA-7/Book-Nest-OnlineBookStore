<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="注册 - Book Nest" scope="request"/>
<%@ include file="common/header.jspf" %>

<section class="auth-panel reveal">
    <div class="auth-copy">
        <span class="eyebrow">Create account</span>
        <h1>创建账号，保存你的选书节奏。</h1>
        <p>注册后可以登录浏览书店、维护个人书单，并追踪订单状态。</p>
    </div>
    <form class="form-card" action="${pageContext.request.contextPath}/register" method="post">
        <c:if test="${not empty error}"><div class="form-error">${error}</div></c:if>
        <label>用户名<input name="username" value="${username}" minlength="3" required></label>
        <label>邮箱<input name="email" type="email" value="${email}" required></label>
        <label>密码<input name="password" type="password" minlength="6" required></label>
        <label>确认密码<input name="confirmPassword" type="password" minlength="6" required></label>
        <button class="primary-button full" type="submit">注册</button>
        <p class="form-note">已有账号？<a href="${pageContext.request.contextPath}/login">去登录</a></p>
    </form>
</section>

<%@ include file="common/footer.jspf" %>
