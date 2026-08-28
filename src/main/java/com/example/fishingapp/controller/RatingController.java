// src/main/java/com/example/fishingapp/controller/RatingController.java
package com.example.fishingapp.controller;

import com.example.fishingapp.dto.RatingResponse;
import com.example.fishingapp.service.RatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/rating")
public class RatingController {

    @Autowired
    private RatingService ratingService;

    @GetMapping("/top100")
    public List<RatingResponse> getTop100() {
        return ratingService.getTop100ByMaxWeight();
    }

    @GetMapping("/top100/nearby")
    public List<RatingResponse> getTop100Nearby(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "50") double radiusKm) {
        return ratingService.getTop100ByMaxWeightInArea(lat, lng, radiusKm);
    }
}