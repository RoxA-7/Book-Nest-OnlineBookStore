package com.bookstore.model;

import java.math.BigDecimal;
import java.util.List;

public class CartSummary {
    private final List<CartItem> items;
    private final int totalQuantity;
    private final BigDecimal totalPrice;

    public CartSummary(List<CartItem> items, int totalQuantity, BigDecimal totalPrice) {
        this.items = items;
        this.totalQuantity = totalQuantity;
        this.totalPrice = totalPrice;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }
}

