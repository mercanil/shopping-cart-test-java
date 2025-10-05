package com.siriusxm.example.cart.model;

import com.siriusxm.example.cart.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CartTest {

    private CartService cartService;

    @BeforeEach
    void setUp() {
        cartService = new CartService("0.125");
    }

    @Test
    void testSampleCalculationFromRequirements() {
        Product cornflakes = new Product("Corn Flakes", 2.52);
        Product weetabix = new Product("Weetabix", 9.98);

        Cart cart = new Cart();
        cart = cartService.addItem(cart, new CartItem(cornflakes, 2));
        cart = cartService.addItem(cart, new CartItem(weetabix, 1));

        CartTotals totals = cartService.calculateTotals(cart);

        assertEquals(15.02, totals.subtotal(), 0.01);
        assertEquals(1.88, totals.tax(), 0.01);
        assertEquals(16.90, totals.total(), 0.01);
    }

    @Test
    void testEmptyCart() {
        Cart cart = new Cart();

        assertTrue(cart.isEmpty());
        assertEquals(0, cart.getItemCount());
        assertEquals(0.0, cartService.calculateSubtotal(cart));
    }

    @Test
    void testAddMultipleProducts() {
        Product cheerios = new Product("Cheerios", 8.43);
        Product frosties = new Product("Frosties", 4.99);

        Cart cart = new Cart();
        cart = cartService.addItem(cart, new CartItem(cheerios, 1));
        cart = cartService.addItem(cart, new CartItem(frosties, 3));

        assertEquals(2, cart.getItemCount());
        assertFalse(cart.isEmpty());
    }

    @Test
    void testCartImmutability() {
        Cart originalCart = new Cart();
        Product shreddies = new Product("Shreddies", 3.0);

        Cart newCart = cartService.addItem(originalCart, new CartItem(shreddies, 1));

        assertTrue(originalCart.isEmpty());
        assertEquals(0, originalCart.getItemCount());

        assertFalse(newCart.isEmpty());
        assertEquals(1, newCart.getItemCount());
    }

    @Test
    void testMultipleQuantities() {
        Product cheerios = new Product("Cheerios", 8.43);

        Cart cart = new Cart();
        cart = cartService.addItem(cart, new CartItem(cheerios, 5));

        double expectedSubtotal = cheerios.price() * 5;
        assertEquals(expectedSubtotal, cartService.calculateSubtotal(cart), 0.01);
    }

    @Test
    void testAddNullItemThrowsException() {
        Cart cart = new Cart();
        assertThrows(IllegalArgumentException.class,
                () -> cartService.addItem(cart, null));
    }

    @Test
    void testAddItemToNullCartThrowsException() {
        Product product = new Product("Test", 10.0);
        assertThrows(IllegalArgumentException.class,
                () -> cartService.addItem(null, new CartItem(product, 1)));
    }

    @Test
    void testCalculateSubtotalWithNullCartThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> cartService.calculateSubtotal(null));
    }

    @Test
    void testCalculateTotalsWithNullCartThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> cartService.calculateTotals(null));
    }
}