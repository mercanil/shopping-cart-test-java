package com.siriusxm.example.cart.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.siriusxm.example.cart.exception.ProductFetchException;
import com.siriusxm.example.cart.model.Product;
import io.vavr.control.Try;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Locale;

@Service
public class ProductPricingService {

    @Autowired
    private  HttpClient httpClient;

    @Value("${cart.pricing.base-url}")
    private String baseUrl;


    public Try<Product> fetchProduct(String productName) {
        return Try.of(() -> {
            String url = baseUrl + productName.toLowerCase(Locale.ROOT) + ".json";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                throw new ProductFetchException(
                        "Failed to fetch product: " + productName +
                                ", status: " + response.statusCode() +
                                ", url: " + url
                );
            }

            return parseProduct(response.body());
        });
    }

    public Product getProduct(String productName) {
        return fetchProduct(productName)
                .getOrElseThrow(ex -> new ProductFetchException(
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