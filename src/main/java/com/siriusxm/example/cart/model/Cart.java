package com.siriusxm.example.cart.model;

import java.util.List;

public record Cart(List<CartItem> items) {

    public Cart() {
        this(List.of());
    }

    public Cart {
        items = List.copyOf(items); // defensive copy, creates unmodifiable list
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public int getItemCount() {
        return items.size();
    }

    public List<CartItem> getItems() {
        return items; // already unmodifiable from constructor
    }
}