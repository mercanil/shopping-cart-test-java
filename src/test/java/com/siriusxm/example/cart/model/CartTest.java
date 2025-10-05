package com.siriusxm.example.cart.model;

import com.siriusxm.example.cart.config.CartConfiguration;
import com.siriusxm.example.cart.service.CartService;
import com.siriusxm.example.cart.service.ProductPricingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CartTest {

    @Autowired
    private ProductPricingService pricingService;

    @Autowired
    private CartService cartService;

    @Test
    void testSampleCalculationFromRequirements() {
        Product cornflakes = pricingService.fetchProduct("cornflakes")
                .getOrElseThrow(ex -> new RuntimeException("Failed to fetch cornflakes", ex));
        Product weetabix = pricingService.fetchProduct("weetabix")
                .getOrElseThrow(ex -> new RuntimeException("Failed to fetch weetabix", ex));

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
        Product cheerios = pricingService.fetchProduct("cheerios")
                .getOrElseThrow(ex -> new RuntimeException("Failed to fetch cheerios", ex));
        Product frosties = pricingService.fetchProduct("frosties")
                .getOrElseThrow(ex -> new RuntimeException("Failed to fetch frosties", ex));

        Cart cart = new Cart();
        cart = cartService.addItem(cart, new CartItem(cheerios, 1));
        cart = cartService.addItem(cart, new CartItem(frosties, 3));

        assertEquals(2, cart.getItemCount());
        assertFalse(cart.isEmpty());
    }

    @Test
    void testCartImmutability() {
        Cart originalCart = new Cart();
        Product shreddies = pricingService.fetchProduct("shreddies")
                .getOrElseThrow(ex -> new RuntimeException("Failed to fetch shreddies", ex));

        Cart newCart = cartService.addItem(originalCart, new CartItem(shreddies, 1));

        assertTrue(originalCart.isEmpty());
        assertEquals(0, originalCart.getItemCount());

        assertFalse(newCart.isEmpty());
        assertEquals(1, newCart.getItemCount());
    }

    @Test
    void testMultipleQuantities() {
        Product cheerios = pricingService.fetchProduct("cheerios")
                .getOrElseThrow(ex -> new RuntimeException("Failed to fetch cheerios", ex));

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
        Product product = pricingService.fetchProduct("cornflakes")
                .getOrElseThrow(ex -> new RuntimeException("Failed to fetch cornflakes", ex));
        assertThrows(IllegalArgumentException.class,
                () -> cartService.addItem(null, new CartItem(product, 1)));
    }
}