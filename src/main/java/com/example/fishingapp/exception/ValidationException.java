// src/main/java/com/example/fishingapp/exception/ValidationException.java
package com.example.fishingapp.exception;

public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}