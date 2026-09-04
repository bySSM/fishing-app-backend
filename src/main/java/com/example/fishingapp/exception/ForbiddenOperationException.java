// src/main/java/com/example/fishingapp/exception/ForbiddenOperationException.java
package com.example.fishingapp.exception;

public class ForbiddenOperationException extends RuntimeException {
    public ForbiddenOperationException(String message) {
        super(message);
    }
}