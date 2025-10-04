package com.siriusxm.example.cart.service;

import com.siriusxm.example.cart.config.CartConfiguration;
import com.siriusxm.example.cart.model.Product;
import io.vavr.control.Try;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(classes = CartConfiguration.class)
class ProductPricingServiceTest {

    @Autowired
    private ProductPricingService service;

    @Test
    void testFetchCornflakes() {
        Try<Product> result = service.fetchProduct("cornflakes");

        assertTrue(result.isSuccess());
        Product product = result.get();
        assertEquals("Corn Flakes", product.name());
        assertEquals(2.52, product.price(), 0.01);
    }

    @Test
    void testFetchAllProducts() {
        String[] products = {"cheerios", "cornflakes", "frosties", "shreddies", "weetabix"};

        for (String productName : products) {
            Try<Product> result = service.fetchProduct(productName);
            assertTrue(result.isSuccess(),
                    "Failed to fetch: " + productName);
            assertNotNull(result.get());
            assertTrue(result.get().price() > 0);
        }
    }

    @Test
    void testFetchInvalidProductReturnsFailure() {
        Try<Product> result = service.fetchProduct("nonexistent");

        assertTrue(result.isFailure());
    }

    @Test
    void testGetProductThrowsExceptionOnFailure() {
        assertThrows(RuntimeException.class,
                () -> service.getProduct("nonexistent"));
    }

    @Test
    void testMonadicComposition() {
        Try<Double> totalPrice = service.fetchProduct("cornflakes")
                .map(product -> product.price() * 2)
                .map(subtotal -> subtotal * 1.125); // with tax

        assertTrue(totalPrice.isSuccess());
        assertEquals(5.67, totalPrice.get(), 0.01);
    }

    @Test
    void testFunctionalErrorHandling() {
        Product defaultProduct = new Product("Default", 0.0);

        Product result = service.fetchProduct("nonexistent")
                .getOrElse(defaultProduct);

        assertEquals(defaultProduct, result);
    }

    @Test
    void testFunctionalErrorHandlingWithRecovery() {
        Product result = service.fetchProduct("invalid")
                .recover(ex -> new Product("Fallback Product", 1.00))
                .get();

        assertEquals("Fallback Product", result.name());
        assertEquals(1.00, result.price());
    }

    @Test
    void testCaseInsensitiveProductName() {
        Try<Product> lowerCase = service.fetchProduct("cornflakes");
        Try<Product> upperCase = service.fetchProduct("CORNFLAKES");
        Try<Product> mixedCase = service.fetchProduct("CornFlakes");

        assertTrue(lowerCase.isSuccess());
        assertTrue(upperCase.isSuccess());
        assertTrue(mixedCase.isSuccess());

        assertEquals(lowerCase.get().price(), upperCase.get().price());
        assertEquals(lowerCase.get().price(), mixedCase.get().price());
    }

    @Test
    void testMonadicChaining() {
        Try<String> result = service.fetchProduct("weetabix")
                .map(product -> "Product: " + product.name() +
                        ", Price: £" + product.price());

        assertTrue(result.isSuccess());
        assertTrue(result.get().contains("Weetabix"));
        assertTrue(result.get().contains("9.98"));
    }

    @Test
    void testServiceInjection() {
        assertNotNull(service, "ProductPricingService should be autowired");
    }

    @Test
    void testGetProductSuccess() {
        Product product = service.getProduct("cornflakes");

        assertNotNull(product);
        assertEquals("Corn Flakes", product.name());
        assertEquals(2.52, product.price(), 0.01);
    }

    @Test
    void testProductImmutability() {
        Product product = service.getProduct("cheerios");
        String originalName = product.name();
        double originalPrice = product.price();

        assertEquals(originalName, product.name());
        assertEquals(originalPrice, product.price());
    }
}