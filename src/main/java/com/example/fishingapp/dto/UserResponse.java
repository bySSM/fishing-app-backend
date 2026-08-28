// src/main/java/com/example/fishingapp/dto/UserResponse.java
package com.example.fishingapp.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private LocalDateTime createdAt;
    private Integer rating;
}