package com.siriusxm.example.cart.model;

import com.siriusxm.example.cart.config.CartConfiguration;
import com.siriusxm.example.cart.service.CartService;
import com.siriusxm.example.cart.service.ProductPricingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = CartConfiguration.class)
class CartTest {

    @Autowired
    private ProductPricingService pricingService;

    @Autowired
    private CartService cartService;

    @Test
    void testSampleCalculationFromRequirements() {
        Product cornflakes = pricingService.getProduct("cornflakes");
        Product weetabix = pricingService.getProduct("weetabix");

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
        Product cheerios = pricingService.getProduct("cheerios");
        Product frosties = pricingService.getProduct("frosties");

        Cart cart = new Cart();
        cart = cartService.addItem(cart, new CartItem(cheerios, 1));
        cart = cartService.addItem(cart, new CartItem(frosties, 3));

        assertEquals(2, cart.getItemCount());
        assertFalse(cart.isEmpty());
    }

    @Test
    void testCartImmutability() {
        Cart originalCart = new Cart();
        Product shreddies = pricingService.getProduct("shreddies");

        Cart newCart = cartService.addItem(originalCart, new CartItem(shreddies, 1));

        assertTrue(originalCart.isEmpty());
        assertEquals(0, originalCart.getItemCount());

        assertFalse(newCart.isEmpty());
        assertEquals(1, newCart.getItemCount());
    }

    @Test
    void testMultipleQuantities() {
        Product cheerios = pricingService.getProduct("cheerios");

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
        Product product = pricingService.getProduct("cornflakes");
        assertThrows(IllegalArgumentException.class,
                () -> cartService.addItem(null, new CartItem(product, 1)));
    }
}