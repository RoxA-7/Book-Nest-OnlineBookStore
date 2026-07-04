<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Book Nest - 在线书店" scope="request"/>
<%@ include file="common/header.jspf" %>

<section class="hero reveal">
    <div class="hero-copy">
        <span class="eyebrow">Clean reading, quiet shopping</span>
        <h1>把想读的书，放进一张轻盈的书单。</h1>
        <p>按分类浏览课程、技术、文学与商业书籍。界面保持克制、清晰、柔和，让选书过程更像翻开一本新书。</p>
        <div class="hero-actions">
            <a class="primary-button" href="#catalog">浏览图书</a>
            <a class="ghost-button" href="${pageContext.request.contextPath}/cart">查看书单</a>
        </div>
    </div>
    <div class="hero-stack" aria-hidden="true">
        <c:forEach items="${featuredBooks}" var="book" varStatus="status">
            <article class="floating-book book-${status.index}">
                <div class="mini-cover" style="background:${book.coverColor}">
                    <span>${fn:substring(book.title, 0, 1)}</span>
                </div>
                <div>
                    <strong>${book.title}</strong>
                    <small>${book.author}</small>
                </div>
            </article>
        </c:forEach>
    </div>
</section>

<section class="toolbar reveal" id="catalog">
    <div>
        <span class="eyebrow">Catalog</span>
        <h2>图书目录</h2>
    </div>
    <a class="ghost-button" href="${pageContext.request.contextPath}/cart">查看书单</a>
</section>

<section class="category-strip reveal" aria-label="图书分类">
    <a class="${empty selectedCategory ? 'active' : ''}" href="${pageContext.request.contextPath}/books">全部</a>
    <c:forEach items="${categories}" var="category">
        <c:url var="categoryUrl" value="/books">
            <c:param name="category" value="${category}"/>
            <c:if test="${not empty keyword}">
                <c:param name="keyword" value="${keyword}"/>
            </c:if>
        </c:url>
        <a class="${selectedCategory == category ? 'active' : ''}" href="${categoryUrl}">${category}</a>
    </c:forEach>
</section>

<section class="stats-row reveal" aria-label="书店概览">
    <div>
        <strong>${bookCount}</strong>
        <span>本在架图书</span>
    </div>
    <div>
        <strong>${categories.size()}</strong>
        <span>个分类</span>
    </div>
    <div>
        <strong>${books.size()}</strong>
        <span>当前筛选结果</span>
    </div>
</section>

<section class="book-grid">
    <c:forEach items="${books}" var="book">
        <article class="book-card reveal">
            <div class="book-cover" style="background:${book.coverColor}">
                <span>${fn:substring(book.title, 0, 1)}</span>
                <small>${book.category}</small>
            </div>
            <div class="book-info">
                <div class="book-meta">
                    <span>${book.category}</span>
                    <c:if test="${book.featured}"><span>精选</span></c:if>
                </div>
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
                        <input type="hidden" name="bookId" value="${book.id}">
                        <input type="hidden" name="returnTo" value="${pageContext.request.contextPath}/books">
                        <button class="ghost-button compact" type="submit">
                            <c:choose>
                                <c:when test="${not empty favoriteBookIds && favoriteBookIds.contains(book.id)}">已收藏</c:when>
                                <c:otherwise>收藏</c:otherwise>
                            </c:choose>
                        </button>
                    </form>
                </div>
                <form action="${pageContext.request.contextPath}/cart" method="post">
                    <input type="hidden" name="action" value="add">
                    <input type="hidden" name="bookId" value="${book.id}">
                    <button class="primary-button full" type="submit" <c:if test="${book.stock <= 0}">disabled</c:if>>加入书单</button>
                </form>
            </div>
        </article>
    </c:forEach>
</section>

<c:if test="${not empty recentHistory}">
    <section class="toolbar reveal">
        <div>
            <span class="eyebrow">Recent</span>
            <h2>最近浏览</h2>
        </div>
        <a class="ghost-button" href="${pageContext.request.contextPath}/insights">查看统计</a>
    </section>
    <section class="history-strip reveal">
        <c:forEach items="${recentHistory}" var="record">
            <a class="history-pill" href="${pageContext.request.contextPath}/book?id=${record.book.id}">
                <strong>${record.book.title}</strong>
                <span>${record.lastViewedText}</span>
            </a>
        </c:forEach>
    </section>
</c:if>

<c:if test="${empty books}">
    <section class="empty-state reveal">
        <h2>没有找到匹配的图书</h2>
        <p>换一个关键词或分类试试。</p>
        <a class="primary-button compact" href="${pageContext.request.contextPath}/books">回到全部图书</a>
    </section>
</c:if>

<%@ include file="common/footer.jspf" %>
