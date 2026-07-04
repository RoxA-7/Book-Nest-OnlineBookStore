<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="我的收藏 - Book Nest" scope="request"/>
<%@ include file="common/header.jspf" %>

<section class="toolbar reveal">
    <div>
        <span class="eyebrow">Favorites</span>
        <h1>我的收藏</h1>
    </div>
    <a class="ghost-button" href="${pageContext.request.contextPath}/books">继续选书</a>
</section>

<c:choose>
    <c:when test="${empty favorites}">
        <section class="empty-state reveal">
            <h2>收藏夹还是空的</h2>
            <p>看到想稍后再买的书，可以先放进收藏夹。</p>
            <a class="primary-button compact" href="${pageContext.request.contextPath}/books">去浏览</a>
        </section>
    </c:when>
    <c:otherwise>
        <section class="book-grid">
            <c:forEach items="${favorites}" var="book">
                <article class="book-card reveal">
                    <div class="book-cover" style="background:${book.coverColor}">
                        <span>${fn:substring(book.title, 0, 1)}</span>
                        <small>${book.category}</small>
                    </div>
                    <div class="book-info">
                        <h3>${book.title}</h3>
                        <p class="author">${book.author}</p>
                        <p class="description">${book.description}</p>
                        <div class="book-bottom">
                            <strong>￥<fmt:formatNumber value="${book.price}" minFractionDigits="2"/></strong>
                            <span>库存 ${book.stock}</span>
                        </div>
                        <div class="book-actions">
                            <a class="ghost-button compact" href="${pageContext.request.contextPath}/book?id=${book.id}">详情</a>
                            <form action="${pageContext.request.contextPath}/favorites" method="post">
                                <input type="hidden" name="action" value="addCart">
                                <input type="hidden" name="bookId" value="${book.id}">
                                <input type="hidden" name="returnTo" value="${pageContext.request.contextPath}/favorites">
                                <button class="primary-button compact" type="submit">加入书单</button>
                            </form>
                        </div>
                        <form action="${pageContext.request.contextPath}/favorites" method="post">
                            <input type="hidden" name="action" value="remove">
                            <input type="hidden" name="bookId" value="${book.id}">
                            <input type="hidden" name="returnTo" value="${pageContext.request.contextPath}/favorites">
                            <button class="text-button danger" type="submit">取消收藏</button>
                        </form>
                    </div>
                </article>
            </c:forEach>
        </section>
    </c:otherwise>
</c:choose>

<%@ include file="common/footer.jspf" %>
