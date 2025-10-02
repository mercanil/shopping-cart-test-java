package com.siriusxm.example.cart.service;

import com.siriusxm.example.cart.model.Cart;
import com.siriusxm.example.cart.model.CartItem;
import com.siriusxm.example.cart.model.CartTotals;
import com.siriusxm.example.cart.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CartServiceTest {

    private CartService cartService;

    @BeforeEach
    void setUp() {
        cartService = new CartService(0.125);
    }

    @Test
    void testCalculateLineTotal() {
        Product product = new Product("Test", 2.50);
        CartItem item = new CartItem(product, 4);

        assertEquals(10.00, cartService.calculateLineTotal(item), 0.01);
    }

    @Test
    void testCalculateSubtotal() {
        Product p1 = new Product("Product 1", 10.00);
        Product p2 = new Product("Product 2", 5.50);

        Cart cart = new Cart();
        cart = cartService.addItem(cart, new CartItem(p1, 2));
        cart = cartService.addItem(cart, new CartItem(p2, 3));

        double expectedSubtotal = (10.00 * 2) + (5.50 * 3);
        assertEquals(expectedSubtotal, cartService.calculateSubtotal(cart), 0.01);
    }

    @Test
    void testCalculateTax() {
        double subtotal = 100.00;
        double tax = cartService.calculateTax(subtotal);

        assertEquals(12.50, tax, 0.01);
    }

    @Test
    void testCalculateTotals() {
        Product product = new Product("Test", 100.00);
        Cart cart = new Cart();
        cart = cartService.addItem(cart, new CartItem(product, 1));

        CartTotals totals = cartService.calculateTotals(cart);

        assertEquals(100.00, totals.subtotal(), 0.01);
        assertEquals(12.50, totals.tax(), 0.01);
        assertEquals(112.50, totals.total(), 0.01);
    }

    @Test
    void testRoundingUp() {
        Product product = new Product("Test", 10.004);
        Cart cart = new Cart();
        cart = cartService.addItem(cart, new CartItem(product, 1));

        CartTotals totals = cartService.calculateTotals(cart);

        assertEquals(10.01, totals.subtotal(), 0.001);
        assertEquals(1.26, totals.tax(), 0.001);
    }

    @Test
    void testAddItemImmutability() {
        Cart originalCart = new Cart();
        Product product = new Product("Test", 5.00);

        Cart newCart = cartService.addItem(originalCart, new CartItem(product, 1));

        assertTrue(originalCart.isEmpty());
        assertEquals(1, newCart.getItemCount());
    }
}