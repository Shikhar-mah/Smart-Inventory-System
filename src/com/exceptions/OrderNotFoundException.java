package com.exceptions;

public class OrderNotFoundException extends Exception {
    private static String message;

    public OrderNotFoundException(String essage) {
        super(message);
    }
}

