// src/main/java/com/example/fishingapp/controller/AquariumController.java
package com.example.fishingapp.controller;

import com.example.fishingapp.dto.AquariumResponse;
import com.example.fishingapp.dto.CatchResponse;
import com.example.fishingapp.model.User;
import com.example.fishingapp.service.CatchService;
import com.example.fishingapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/aquarium")
public class AquariumController {

    @Autowired
    private CatchService catchService;

    @Autowired
    private UserService userService;

    @GetMapping("/my")
    public ResponseEntity<?> getMyAquarium() {
        String username = getCurrentUsername();
        User user = userService.findByUsername(username);
        return getAquarium(user.getId());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserAquarium(@PathVariable Long userId) {
        return getAquarium(userId);
    }

    private ResponseEntity<?> getAquarium(Long userId) {
        User user = userService.findById(userId);
        List<CatchResponse> topFishes = catchService.getTopFishes(userId, 5);

        AquariumResponse response = new AquariumResponse();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setTopFishes(topFishes);
        response.setRating(user.getRating());

        return ResponseEntity.ok(response);
    }

    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}