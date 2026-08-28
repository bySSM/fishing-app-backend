// src/main/java/com/example/fishingapp/dto/CatchRequest.java
package com.example.fishingapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CatchRequest {

    @NotBlank(message = "Fish type is required")
    private String fishType;

    private Double weight;
    private Double length;

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

    private String bait;
    private String description;
    private String photoUrl;
    private Boolean isLocationHidden = false;
}