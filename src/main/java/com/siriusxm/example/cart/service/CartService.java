package com.siriusxm.example.cart.service;

import com.siriusxm.example.cart.model.Cart;
import com.siriusxm.example.cart.model.CartItem;
import com.siriusxm.example.cart.model.CartTotals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {

    private static final Logger log = LoggerFactory.getLogger(CartService.class);

    private final BigDecimal taxRate;

    public CartService(@Value("${cart.tax.rate}") double taxRate) {
        if (taxRate < 0 || taxRate > 1) {
            throw new IllegalArgumentException("Tax rate must be between 0 and 1");
        }
        this.taxRate = BigDecimal.valueOf(taxRate);
        log.info("CartService initialized with tax rate: {}", taxRate);
    }

    public Cart addItem(Cart cart, CartItem item) {
        if (cart == null) {
            log.error("Attempted to add item to null cart");
            throw new IllegalArgumentException("Cart cannot be null");
        }
        if (item == null) {
            log.error("Attempted to add null item to cart");
            throw new IllegalArgumentException("CartItem cannot be null");
        }

        List<CartItem> newItems = new ArrayList<>(cart.getItems());
        newItems.add(item);
        log.debug("Added item {} with quantity {} to cart", item.product().name(), item.quantity());
        return new Cart(newItems);
    }

    public double calculateItemTotal(CartItem item) {
        if (item == null) {
            throw new IllegalArgumentException("CartItem cannot be null");
        }
        return item.product().price() * item.quantity();
    }

    public double calculateSubtotal(Cart cart) {
        if (cart == null) {
            throw new IllegalArgumentException("Cart cannot be null");
        }
        double subtotal = cart.getItems().stream()
                .mapToDouble(this::calculateItemTotal)
                .sum();
        log.debug("Calculated subtotal: {} for {} items", subtotal, cart.getItemCount());
        return subtotal;
    }

    public CartTotals calculateTotals(Cart cart) {
        if (cart == null) {
            throw new IllegalArgumentException("Cart cannot be null");
        }

        double subtotal = calculateSubtotal(cart);

        BigDecimal subtotalBd = BigDecimal.valueOf(subtotal)
                .setScale(2, RoundingMode.CEILING);

        BigDecimal taxBd = subtotalBd.multiply(taxRate)
                .setScale(2, RoundingMode.CEILING);

        BigDecimal totalBd = subtotalBd.add(taxBd)
                .setScale(2, RoundingMode.CEILING);

        CartTotals totals = new CartTotals(
                subtotalBd.doubleValue(),
                taxBd.doubleValue(),
                totalBd.doubleValue()
        );

        log.info("Calculated cart totals - Subtotal: {}, Tax: {}, Total: {}",
                totals.subtotal(), totals.tax(), totals.total());

        return totals;
    }

    public double calculateTax(double subtotal) {
        if (subtotal < 0) {
            throw new IllegalArgumentException("Subtotal cannot be negative");
        }
        return BigDecimal.valueOf(subtotal)
                .multiply(taxRate)
                .setScale(2, RoundingMode.CEILING)
                .doubleValue();
    }

    public double getTaxRate() {
        return taxRate.doubleValue();
    }
}