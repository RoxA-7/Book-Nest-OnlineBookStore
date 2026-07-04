<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="登录 - Book Nest" scope="request"/>
<%@ include file="common/header.jspf" %>

<section class="auth-panel reveal">
    <div class="auth-copy">
        <span class="eyebrow">Welcome back</span>
        <h1>登录后继续整理你的阅读书单。</h1>
        <p>登录后可以保存购物车、完成结算，并查看自己的历史订单和订单状态。</p>
    </div>
    <form class="form-card" action="${pageContext.request.contextPath}/login" method="post">
        <c:if test="${not empty error}"><div class="form-error">${error}</div></c:if>
        <label>用户名<input name="username" value="${username}" required></label>
        <label>密码<input name="password" type="password" required></label>
        <button class="primary-button full" type="submit">登录</button>
        <p class="form-note">还没有账号？<a href="${pageContext.request.contextPath}/register">创建一个</a></p>
    </form>
</section>

<%@ include file="common/footer.jspf" %>
