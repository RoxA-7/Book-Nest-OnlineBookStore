package com.bookstore.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BrowsingRecord {
    private Book book;
    private int viewCount;
    private LocalDateTime lastViewed;

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public LocalDateTime getLastViewed() {
        return lastViewed;
    }

    public void setLastViewed(LocalDateTime lastViewed) {
        this.lastViewed = lastViewed;
    }

    public String getLastViewedText() {
        if (lastViewed == null) {
            return "";
        }
        return lastViewed.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
