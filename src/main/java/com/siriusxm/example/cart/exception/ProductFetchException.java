package com.siriusxm.example.cart.exception;

public class ProductFetchException extends RuntimeException {

    public ProductFetchException(String message) {
        super(message);
    }

    public ProductFetchException(String message, Throwable cause) {
        super(message, cause);
    }
}