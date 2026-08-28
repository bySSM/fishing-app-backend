// src/main/java/com/example/fishingapp/dto/RatingResponse.java
package com.example.fishingapp.dto;

import lombok.Data;

@Data
public class RatingResponse {
    private Long userId;
    private String username;
    private Double maxWeight;
    private Double totalWeight;
    private Integer fishCount;
    private Integer likesCount;
    private Integer rating;
}