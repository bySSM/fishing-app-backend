// src/main/java/com/example/fishingapp/dto/AquariumResponse.java
package com.example.fishingapp.dto;

import lombok.Data;
import java.util.List;

@Data
public class AquariumResponse {
    private Long userId;
    private String username;
    private List<CatchResponse> topFishes;
    private Integer totalCatches;
    private Integer rating;
}