<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="${book.title} - Book Nest" scope="request"/>
<%@ include file="common/header.jspf" %>

<section class="detail-layout reveal">
    <div class="detail-cover" style="background:${book.coverColor}">
        <span>${fn:substring(book.title, 0, 1)}</span>
    </div>
    <article class="detail-copy">
        <span class="eyebrow">${book.category}</span>
        <h1>${book.title}</h1>
        <p class="detail-author">${book.author}</p>
        <p>${book.description}</p>
        <div class="detail-meta">
            <div><strong>￥<fmt:formatNumber value="${book.price}" minFractionDigits="2"/></strong><span>当前价格</span></div>
            <div><strong>${book.stock}</strong><span>库存</span></div>
            <div><strong>${book.featured ? 'Yes' : 'No'}</strong><span>精选</span></div>
        </div>
        <div class="detail-actions">
            <form action="${pageContext.request.contextPath}/cart" method="post">
                <input type="hidden" name="action" value="add">
                <input type="hidden" name="bookId" value="${book.id}">
                <button class="primary-button" type="submit" <c:if test="${book.stock <= 0}">disabled</c:if>>加入书单</button>
            </form>
            <form action="${pageContext.request.contextPath}/favorites" method="post">
                <input type="hidden" name="bookId" value="${book.id}">
                <input type="hidden" name="returnTo" value="${pageContext.request.contextPath}/book?id=${book.id}">
                <button class="ghost-button" type="submit">
                    <c:choose>
                        <c:when test="${not empty favoriteBookIds && favoriteBookIds.contains(book.id)}">已收藏</c:when>
                        <c:otherwise>加入收藏</c:otherwise>
                    </c:choose>
                </button>
            </form>
            <a class="text-button" href="${pageContext.request.contextPath}/books">返回书店</a>
        </div>
    </article>
</section>

<section class="toolbar reveal">
    <div>
        <span class="eyebrow">More in ${book.category}</span>
        <h2>同标签推荐</h2>
    </div>
</section>

<section class="book-grid related-grid">
    <c:forEach items="${relatedBooks}" var="related">
        <article class="book-card reveal">
            <div class="book-cover" style="background:${related.coverColor}">
                <span>${fn:substring(related.title, 0, 1)}</span>
                <small>${related.category}</small>
            </div>
            <div class="book-info">
                <h3>${related.title}</h3>
                <p class="author">${related.author}</p>
                <p class="description">${related.description}</p>
                <div class="book-bottom">
                    <strong>￥<fmt:formatNumber value="${related.price}" minFractionDigits="2"/></strong>
                    <span>库存 ${related.stock}</span>
                </div>
                <a class="ghost-button full" href="${pageContext.request.contextPath}/book?id=${related.id}">查看详情</a>
            </div>
        </article>
    </c:forEach>
</section>

<c:if test="${empty relatedBooks}">
    <section class="empty-state reveal">
        <h2>暂无同标签推荐</h2>
        <p>这个分类下目前只有这一本书。</p>
    </section>
</c:if>

<%@ include file="common/footer.jspf" %>
