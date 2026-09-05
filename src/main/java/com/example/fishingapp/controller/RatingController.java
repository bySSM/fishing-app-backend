// src/main/java/com/example/fishingapp/controller/RatingController.java
package com.example.fishingapp.controller;

import com.example.fishingapp.dto.RatingResponse;
import com.example.fishingapp.service.RatingService;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/rating")
@Validated
public class RatingController {

    @Autowired
    private RatingService ratingService;

    @GetMapping("/top100")
    public List<RatingResponse> getTop100() {
        return ratingService.getTop100ByMaxWeight();
    }

    @GetMapping("/top100/nearby")
    public List<RatingResponse> getTop100Nearby(
            @RequestParam
            @DecimalMin(value = "-90", message = "Latitude must be between -90 and 90")
            @DecimalMax(value = "90", message = "Latitude must be between -90 and 90")
            double lat,

            @RequestParam
            @DecimalMin(value = "-180", message = "Longitude must be between -180 and 180")
            @DecimalMax(value = "180", message = "Longitude must be between -180 and 180")
            double lng,

            @RequestParam(defaultValue = "50")
            @DecimalMin(value = "0.1", message = "radiusKm must be greater than 0")
            @DecimalMax(value = "20000", message = "radiusKm must not exceed 20000 (whole Earth)")
            double radiusKm) {

        return ratingService.getTop100ByMaxWeightInArea(lat, lng, radiusKm);
    }
}