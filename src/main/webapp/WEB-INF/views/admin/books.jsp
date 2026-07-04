<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="后台管理 - Book Nest" scope="request"/>
<%@ include file="../common/header.jspf" %>

<section class="toolbar reveal">
    <div>
        <span class="eyebrow">Admin</span>
        <h1>图书后台管理</h1>
    </div>
    <a class="ghost-button" href="${pageContext.request.contextPath}/admin/orders">查看订单</a>
</section>

<section class="stats-row reveal">
    <div>
        <strong>${bookCount}</strong>
        <span>在架图书</span>
    </div>
    <div>
        <strong>${categories.size()}</strong>
        <span>图书分类</span>
    </div>
    <div>
        <strong>${lowStockCount}</strong>
        <span>低库存</span>
    </div>
</section>

<form class="admin-filter reveal" action="${pageContext.request.contextPath}/admin/books" method="get">
    <input name="keyword" value="${keyword}" placeholder="搜索书名或作者">
    <select name="category">
        <option value="">全部分类</option>
        <c:forEach items="${categories}" var="category">
            <option value="${category}" <c:if test="${selectedCategory == category}">selected</c:if>>${category}</option>
        </c:forEach>
    </select>
    <button class="primary-button compact" type="submit">筛选</button>
    <a class="ghost-button compact" href="${pageContext.request.contextPath}/admin/books">重置</a>
</form>

<c:if test="${not empty error}"><div class="form-error admin-error">${error}</div></c:if>

<section class="admin-layout">
    <div class="admin-side reveal">
        <form class="form-card admin-form" action="${pageContext.request.contextPath}/admin/books" method="post">
            <input type="hidden" name="action" value="create">
            <span class="eyebrow">New book</span>
            <h2>上架新图书</h2>
            <label>书名<input name="title" required></label>
            <label>作者<input name="author" required></label>
            <label>分类标签<input name="category" list="categoryList" placeholder="多个分类用逗号分隔" required></label>
            <label>价格<input name="price" type="number" step="0.01" min="0" required></label>
            <label>库存<input name="stock" type="number" min="0" required></label>
            <input type="hidden" name="coverColor" value="linear-gradient(145deg, #f3b27c, #d96f45)">
            <label>简介<textarea name="description" rows="4" required></textarea></label>
            <label class="check-line"><input name="featured" type="checkbox"> 设为精选</label>
            <button class="primary-button full" type="submit">上架图书</button>
        </form>

        <form class="form-card category-form" action="${pageContext.request.contextPath}/admin/books" method="post">
            <input type="hidden" name="action" value="createCategory">
            <span class="eyebrow">New category</span>
            <h2>添加图书分类</h2>
            <label>分类名称<input name="categoryName" placeholder="例如 Poetry" required></label>
            <label>分类说明<textarea name="categoryDescription" rows="3" placeholder="这个分类主要收录什么书"></textarea></label>
            <button class="ghost-button full" type="submit">添加分类</button>
        </form>
    </div>

    <div class="admin-table reveal">
        <div class="table-head">
            <span>图书</span>
            <span>分类</span>
            <span>价格</span>
            <span>库存</span>
            <span>操作</span>
        </div>
        <c:forEach items="${books}" var="book">
            <form class="table-row" action="${pageContext.request.contextPath}/admin/books" method="post">
                <input type="hidden" name="id" value="${book.id}">
                <div class="book-edit-title">
                    <div class="mini-cover tiny" style="background:${book.coverColor}">
                        <span>${fn:substring(book.title, 0, 1)}</span>
                    </div>
                    <div>
                        <input name="title" value="${book.title}" required>
                        <input name="author" value="${book.author}" required>
                    </div>
                </div>
                <input name="category" value="${book.category}" list="categoryList" title="多个分类用逗号分隔" required>
                <input name="price" type="number" step="0.01" min="0" value="${book.price}" required>
                <input name="stock" type="number" min="0" value="${book.stock}" required>
                <div class="table-actions">
                    <input type="hidden" name="coverColor" value="${book.coverColor}">
                    <label class="mini-check"><input name="featured" type="checkbox" <c:if test="${book.featured}">checked</c:if>> 精选</label>
                    <button class="ghost-button" name="action" value="update" type="submit">保存</button>
                    <button class="text-button danger" name="action" value="delete" type="submit" formnovalidate data-confirm="确定删除这本书吗？">删除</button>
                </div>
                <textarea class="row-description" name="description" rows="2" required>${book.description}</textarea>
            </form>
        </c:forEach>
    </div>
</section>

<datalist id="categoryList">
    <c:forEach items="${categories}" var="category">
        <option value="${category}">
    </c:forEach>
</datalist>

<%@ include file="../common/footer.jspf" %>
