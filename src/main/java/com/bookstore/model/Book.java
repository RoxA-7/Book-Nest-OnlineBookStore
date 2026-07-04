package com.bookstore.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Book {
    private long id;
    private String title;
    private String author;
    private String category;
    private BigDecimal price;
    private int stock;
    private String description;
    private String coverColor;
    private boolean featured;
    private LocalDateTime createdAt;
    private String highlightedTitle;
    private String highlightedAuthor;
    private String highlightedDescription;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCoverColor() {
        return coverColor;
    }

    public void setCoverColor(String coverColor) {
        this.coverColor = coverColor;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getHighlightedTitle() {
        return highlightedTitle == null ? title : highlightedTitle;
    }

    public void setHighlightedTitle(String highlightedTitle) {
        this.highlightedTitle = highlightedTitle;
    }

    public String getHighlightedAuthor() {
        return highlightedAuthor == null ? author : highlightedAuthor;
    }

    public void setHighlightedAuthor(String highlightedAuthor) {
        this.highlightedAuthor = highlightedAuthor;
    }

    public String getHighlightedDescription() {
        return highlightedDescription == null ? description : highlightedDescription;
    }

    public void setHighlightedDescription(String highlightedDescription) {
        this.highlightedDescription = highlightedDescription;
    }
}

