package com.siriusxm.example.cart.config;

import com.siriusxm.example.cart.service.CartService;
import com.siriusxm.example.cart.service.ProductPricingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class CartConfiguration {

    @Value("${cart.pricing.base-url}")
    private String baseUrl;

    @Value("${cart.pricing.timeout-seconds:10}")
    private int timeoutSeconds;

    @Value("${cart.tax.rate}")
    private double taxRate;

    @Bean
    public HttpClient httpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(timeoutSeconds))
                .build();
    }

    @Bean
    public ProductPricingService productPricingService(HttpClient httpClient) {
        return new ProductPricingService(httpClient, baseUrl);
    }

    @Bean
    public CartService cartService() {
        return new CartService(taxRate);
    }
}