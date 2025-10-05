package com.siriusxm.example.cart.service;

import com.siriusxm.example.cart.exception.ProductFetchException;
import com.siriusxm.example.cart.model.Product;
import io.vavr.control.Try;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductPricingServiceUnitTest {

    @Mock
    private HttpClient mockHttpClient;

    @Mock
    private HttpResponse<String> mockResponse;

    private ProductPricingService service;

    @BeforeEach
    void setUp() {
        service = new ProductPricingService(mockHttpClient, "https://example.com/");
    }

    @Test
    void testFetchProductSuccess() throws Exception {
        String jsonResponse = "{\"title\":\"Corn Flakes\",\"price\":2.52}";
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(jsonResponse);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        Try<Product> result = service.fetchProduct("cornflakes");

        assertTrue(result.isSuccess());
        Product product = result.get();
        assertEquals("Corn Flakes", product.name());
        assertEquals(2.52, product.price(), 0.01);
    }

    @Test
    void testFetchProductWithDifferentPrice() throws Exception {
        String jsonResponse = "{\"title\":\"Weetabix\",\"price\":9.98}";
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(jsonResponse);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        Try<Product> result = service.fetchProduct("weetabix");

        assertTrue(result.isSuccess());
        assertEquals("Weetabix", result.get().name());
        assertEquals(9.98, result.get().price(), 0.01);
    }

    @Test
    void testFetchProduct404ReturnsFailure() throws Exception {
        when(mockResponse.statusCode()).thenReturn(404);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        Try<Product> result = service.fetchProduct("nonexistent");

        assertTrue(result.isFailure());
        assertInstanceOf(ProductFetchException.class, result.getCause());
        assertTrue(result.getCause().getMessage().contains("Failed to fetch product"));
    }

    @Test
    void testFetchProduct500ReturnsFailure() throws Exception {
        when(mockResponse.statusCode()).thenReturn(500);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        Try<Product> result = service.fetchProduct("product");

        assertTrue(result.isFailure());
        assertInstanceOf(ProductFetchException.class, result.getCause());
    }

    @Test
    void testFetchProductNetworkErrorReturnsFailure() throws Exception {
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new RuntimeException("Network error"));

        Try<Product> result = service.fetchProduct("product");

        assertTrue(result.isFailure());
    }

    @Test
    void testFetchProductInvalidJsonReturnsFailure() throws Exception {
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("invalid json");
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        Try<Product> result = service.fetchProduct("product");

        assertTrue(result.isFailure());
        assertInstanceOf(ProductFetchException.class, result.getCause());
    }

    @Test
    void testFetchProductEmptyResponseReturnsFailure() throws Exception {
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("");
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        Try<Product> result = service.fetchProduct("product");

        assertTrue(result.isFailure());
        assertInstanceOf(ProductFetchException.class, result.getCause());
        assertTrue(result.getCause().getMessage().contains("empty response"));
    }

    @Test
    void testFetchProductMissingTitleReturnsFailure() throws Exception {
        String jsonResponse = "{\"price\":2.52}";
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(jsonResponse);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        Try<Product> result = service.fetchProduct("product");

        assertTrue(result.isFailure());
        assertInstanceOf(ProductFetchException.class, result.getCause());
    }

    @Test
    void testFetchProductMissingPriceReturnsFailure() throws Exception {
        String jsonResponse = "{\"title\":\"Product\"}";
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(jsonResponse);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        Try<Product> result = service.fetchProduct("product");

        assertTrue(result.isFailure());
        assertInstanceOf(ProductFetchException.class, result.getCause());
    }

    @Test
    void testFetchNullProductNameReturnsFailure() {
        Try<Product> result = service.fetchProduct(null);

        assertTrue(result.isFailure());
        assertInstanceOf(ProductFetchException.class, result.getCause());
        assertEquals("Product name cannot be null or blank", result.getCause().getMessage());
    }

    @Test
    void testFetchBlankProductNameReturnsFailure() {
        Try<Product> result = service.fetchProduct("   ");

        assertTrue(result.isFailure());
        assertInstanceOf(ProductFetchException.class, result.getCause());
    }

    @Test
    void testCaseInsensitiveProductName() throws Exception {
        String jsonResponse = "{\"title\":\"Corn Flakes\",\"price\":2.52}";
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(jsonResponse);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        Try<Product> lowerCase = service.fetchProduct("cornflakes");
        Try<Product> upperCase = service.fetchProduct("CORNFLAKES");
        Try<Product> mixedCase = service.fetchProduct("CornFlakes");

        assertTrue(lowerCase.isSuccess());
        assertTrue(upperCase.isSuccess());
        assertTrue(mixedCase.isSuccess());

        verify(mockHttpClient, times(3)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void testMonadicComposition() throws Exception {
        String jsonResponse = "{\"title\":\"Corn Flakes\",\"price\":2.52}";
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(jsonResponse);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        Try<Double> totalPrice = service.fetchProduct("cornflakes")
                .map(product -> product.price() * 2)
                .map(subtotal -> subtotal * 1.125);

        assertTrue(totalPrice.isSuccess());
        assertEquals(5.67, totalPrice.get(), 0.01);
    }

    @Test
    void testFunctionalErrorHandling() throws Exception {
        when(mockResponse.statusCode()).thenReturn(404);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        Product defaultProduct = new Product("Default", 0.0);

        Product result = service.fetchProduct("nonexistent")
                .getOrElse(defaultProduct);

        assertEquals(defaultProduct, result);
    }

    @Test
    void testFunctionalErrorHandlingWithRecovery() throws Exception {
        when(mockResponse.statusCode()).thenReturn(404);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        Product result = service.fetchProduct("invalid")
                .recover(ex -> new Product("Fallback Product", 1.00))
                .get();

        assertEquals("Fallback Product", result.name());
        assertEquals(1.00, result.price());
    }

    @Test
    void testConstructorValidation() {
        assertThrows(IllegalArgumentException.class,
                () -> new ProductPricingService(null, "https://example.com/"));

        assertThrows(IllegalArgumentException.class,
                () -> new ProductPricingService(mockHttpClient, null));

        assertThrows(IllegalArgumentException.class,
                () -> new ProductPricingService(mockHttpClient, ""));

        assertThrows(IllegalArgumentException.class,
                () -> new ProductPricingService(mockHttpClient, "   "));
    }
}