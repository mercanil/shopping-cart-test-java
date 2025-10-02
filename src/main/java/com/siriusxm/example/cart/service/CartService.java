package com.siriusxm.example.cart.service;

import com.siriusxm.example.cart.model.Cart;
import com.siriusxm.example.cart.model.CartItem;
import com.siriusxm.example.cart.model.CartTotals;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {

    private final double taxRate;

    public CartService(@Value("${cart.tax.rate}") double taxRate) {
        this.taxRate = taxRate;
    }

    public Cart addItem(Cart cart, CartItem item) {
        if (cart == null) {
            throw new IllegalArgumentException("Cart cannot be null");
        }
        if (item == null) {
            throw new IllegalArgumentException("CartItem cannot be null");
        }

        List<CartItem> newItems = new ArrayList<>(cart.getItems());
        newItems.add(item);
        return new Cart(newItems);
    }

    public double calculateLineTotal(CartItem item) {
        return item.product().price() * item.quantity();
    }

    public double calculateSubtotal(Cart cart) {
        return cart.getItems().stream()
                .mapToDouble(this::calculateLineTotal)
                .sum();
    }

    public CartTotals calculateTotals(Cart cart) {
        double subtotal = calculateSubtotal(cart);

        BigDecimal subtotalBd = BigDecimal.valueOf(subtotal)
                .setScale(2, RoundingMode.CEILING);

        BigDecimal taxBd = subtotalBd.multiply(BigDecimal.valueOf(taxRate))
                .setScale(2, RoundingMode.CEILING);

        BigDecimal totalBd = subtotalBd.add(taxBd)
                .setScale(2, RoundingMode.CEILING);

        return new CartTotals(
                subtotalBd.doubleValue(),
                taxBd.doubleValue(),
                totalBd.doubleValue()
        );
    }

    public double calculateTax(double subtotal) {
        return BigDecimal.valueOf(subtotal)
                .multiply(BigDecimal.valueOf(taxRate))
                .setScale(2, RoundingMode.CEILING)
                .doubleValue();
    }

    public double getTaxRate() {
        return taxRate;
    }
}