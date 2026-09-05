// src/main/java/com/example/fishingapp/dto/CatchRequest.java
package com.example.fishingapp.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CatchRequest {

    @NotBlank(message = "Fish type is required")
    private String fishType;

    @DecimalMin(value = "0.01", message = "Weight must be greater than 0")
    @DecimalMax(value = "1000", message = "Weight seems unrealistic (max 1000 kg)")
    private Double weight;

    @DecimalMin(value = "0.1", message = "Length must be greater than 0")
    @DecimalMax(value = "500", message = "Length seems unrealistic (max 500 cm)")
    private Double length;

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90", message = "Latitude must be between -90 and 90")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180", message = "Longitude must be between -180 and 180")
    private Double longitude;

    private String bait;
    private String description;
    private String photoUrl;
    private Boolean isLocationHidden = false;
}