package com.siriusxm.example.cart.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.siriusxm.example.cart.exception.ProductFetchException;
import com.siriusxm.example.cart.model.Product;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Locale;

@Service
public class ProductPricingService {

    private static final Logger log = LoggerFactory.getLogger(ProductPricingService.class);
    private static final String JSON_EXTENSION = ".json";
    public static final String TITLE = "title";
    public static final String PRICE = "price";

    private final HttpClient httpClient;
    private final String baseUrl;

    public ProductPricingService(
            HttpClient httpClient,
            @Value("${cart.pricing.base-url}") String baseUrl) {
        if (httpClient == null) {
            throw new IllegalArgumentException("HttpClient cannot be null");
        }
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("Base URL cannot be null or blank");
        }
        if (!baseUrl.endsWith("/")) {
            throw new IllegalArgumentException("Base URL must end with /");
        }
        this.httpClient = httpClient;
        this.baseUrl = baseUrl;
        log.info("ProductPricingService initialized with base URL: {}", baseUrl);
    }

    public Try<Product> fetchProduct(String productName) {
        if (productName == null || productName.isBlank()) {
            log.error("Attempted to fetch product with null or blank name");
            return Try.failure(new ProductFetchException("Product name cannot be null or blank"));
        }

        return Try.of(() -> {
            String url = baseUrl + productName.toLowerCase(Locale.ROOT) + JSON_EXTENSION;
            log.debug("Fetching product from URL: {}", url);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                log.error("Failed to fetch product: {}, status: {}, url: {}",
                         productName, response.statusCode(), url);
                throw new ProductFetchException(
                        "Failed to fetch product: " + productName +
                                ", status: " + response.statusCode() +
                                ", url: " + url
                );
            }

            Product product = parseProduct(response.body());
            log.info("Successfully fetched product: {} with price: {}", product.name(), product.price());
            return product;
        }).onFailure(ex -> {
            if (!(ex instanceof ProductFetchException)) {
                log.error("Unexpected error fetching product: {}", productName, ex);
            }
        });
    }

    public Product getProduct(String productName) {
        return fetchProduct(productName)
                .getOrElseThrow(ex -> {
                    if (ex instanceof ProductFetchException) {
                        throw (ProductFetchException) ex;
                    }
                    return new ProductFetchException("Failed to fetch product: " + productName, ex);
                });
    }

    private Product parseProduct(String jsonResponse) {
        if (jsonResponse == null || jsonResponse.isBlank()) {
            throw new ProductFetchException("Received empty response from pricing service");
        }

        try {
            JsonObject jsonObject = JsonParser.parseString(jsonResponse)
                    .getAsJsonObject();

            String title = jsonObject.get(TITLE).getAsString();
            double price = jsonObject.get(PRICE).getAsDouble();

            return new Product(title, price);
        } catch (Exception e) {
            log.error("Failed to parse product JSON: {}", jsonResponse, e);
            throw new ProductFetchException("Failed to parse product JSON", e);
        }
    }
}