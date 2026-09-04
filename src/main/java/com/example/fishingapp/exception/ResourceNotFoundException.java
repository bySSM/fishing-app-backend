// src/main/java/com/example/fishingapp/exception/ResourceNotFoundException.java
package com.example.fishingapp.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}