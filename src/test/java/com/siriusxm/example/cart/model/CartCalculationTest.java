package com.siriusxm.example.cart.model;

import com.siriusxm.example.cart.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CartCalculationTest {

    private CartService cartService;

    @BeforeEach
    void setUp() {
        cartService = new CartService("0.125");
    }

    @Test
    void testSubtotalCalculation() {
        Product p1 = new Product("Test Product 1", 10.00);
        Product p2 = new Product("Test Product 2", 5.50);

        Cart cart = new Cart();
        cart = cartService.addItem(cart, new CartItem(p1, 2));
        cart = cartService.addItem(cart, new CartItem(p2, 3));

        double expectedSubtotal = (10.00 * 2) + (5.50 * 3);
        assertEquals(expectedSubtotal, cartService.calculateSubtotal(cart), 0.01);
    }

    @Test
    void testTaxCalculation() {
        double subtotal = 100.00;
        double tax = cartService.calculateTax(subtotal);

        assertEquals(12.50, tax, 0.01);
    }

    @Test
    void testTaxRate() {
        double subtotal = 200.00;
        double tax = cartService.calculateTax(subtotal);

        assertEquals(25.00, tax, 0.01);
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
        assertEquals(11.27, totals.total(), 0.001);
    }

    @Test
    void testRoundingUpEdgeCase() {
        Product product = new Product("Test", 15.021);
        Cart cart = new Cart();
        cart = cartService.addItem(cart, new CartItem(product, 1));

        CartTotals totals = cartService.calculateTotals(cart);

        assertEquals(15.03, totals.subtotal(), 0.001);
    }

    @Test
    void testLineTotal() {
        Product product = new Product("Test", 2.50);
        CartItem item = new CartItem(product, 4);

        assertEquals(10.00, cartService.calculateItemTotal(item), 0.01);
    }

    @Test
    void testLineTotalWithDecimalPrice() {
        Product product = new Product("Test", 3.33);
        CartItem item = new CartItem(product, 3);

        assertEquals(9.99, cartService.calculateItemTotal(item), 0.01);
    }

    @Test
    void testZeroSubtotal() {
        Cart cart = new Cart();
        CartTotals totals = cartService.calculateTotals(cart);

        assertEquals(0.0, totals.subtotal());
        assertEquals(0.0, totals.tax());
        assertEquals(0.0, totals.total());
    }

    @Test
    void testProductValidation() {
        assertThrows(IllegalArgumentException.class,
                () -> new Product(null, 10.0));

        assertThrows(IllegalArgumentException.class,
                () -> new Product("", 10.0));

        assertThrows(IllegalArgumentException.class,
                () -> new Product("   ", 10.0));

        assertThrows(IllegalArgumentException.class,
                () -> new Product("Test", -1.0));
    }

    @Test
    void testCartItemValidation() {
        Product product = new Product("Test", 10.0);

        assertThrows(IllegalArgumentException.class,
                () -> new CartItem(null, 1));

        assertThrows(IllegalArgumentException.class,
                () -> new CartItem(product, 0));

        assertThrows(IllegalArgumentException.class,
                () -> new CartItem(product, -1));
    }

    @Test
    void testValidProduct() {
        Product product = new Product("Valid Product", 99.99);

        assertEquals("Valid Product", product.name());
        assertEquals(99.99, product.price());
    }

    @Test
    void testValidCartItem() {
        Product product = new Product("Test", 5.00);
        CartItem item = new CartItem(product, 10);

        assertEquals(product, item.product());
        assertEquals(10, item.quantity());
    }

    @Test
    void testCartWithSingleItem() {
        Product product = new Product("Single", 7.50);
        Cart cart = new Cart();
        cart = cartService.addItem(cart, new CartItem(product, 1));

        assertEquals(1, cart.getItemCount());
        assertEquals(7.50, cartService.calculateSubtotal(cart), 0.01);
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