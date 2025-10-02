package com.siriusxm.example.cart.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.siriusxm.example.cart.model.Product;
import io.vavr.control.Try;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class ProductPricingService {

    private final HttpClient httpClient;
    private final String baseUrl;

    public ProductPricingService(HttpClient httpClient, String baseUrl) {
        this.httpClient = httpClient;
        this.baseUrl = baseUrl;
    }

    public Try<Product> fetchProduct(String productName) {
        return Try.of(() -> {
            String url = baseUrl + productName.toLowerCase() + ".json";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                throw new RuntimeException(
                        "Failed to fetch product: " + productName +
                                ", status: " + response.statusCode()
                );
            }

            return parseProduct(response.body());
        });
    }

    public Product getProduct(String productName) {
        return fetchProduct(productName)
                .getOrElseThrow(ex -> new RuntimeException(
                        "Failed to fetch product: " + productName, ex
                ));
    }

    private Product parseProduct(String jsonResponse) {
        JsonObject jsonObject = JsonParser.parseString(jsonResponse)
                .getAsJsonObject();

        String title = jsonObject.get("title").getAsString();
        double price = jsonObject.get("price").getAsDouble();

        return new Product(title, price);
    }
}