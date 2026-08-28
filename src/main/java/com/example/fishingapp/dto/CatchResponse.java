// src/main/java/com/example/fishingapp/dto/CatchResponse.java
package com.example.fishingapp.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CatchResponse {
    private Long id;
    private String fishType;
    private Double weight;
    private Double length;
    private Double latitude;
    private Double longitude;
    private String bait;
    private String description;
    private String photoUrl;
    private LocalDateTime createdAt;
    private Integer likesCount;
    private Long userId;
    private String username;
    private Boolean isLocationHidden;
}