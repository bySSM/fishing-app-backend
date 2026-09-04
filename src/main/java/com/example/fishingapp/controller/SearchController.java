// src/main/java/com/example/fishingapp/controller/SearchController.java
package com.example.fishingapp.controller;

import com.example.fishingapp.model.User;
import com.example.fishingapp.service.RatingService;
import com.example.fishingapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    @Autowired
    private UserService userService;

    @Autowired
    private RatingService ratingService;

    @GetMapping("/users")
    public ResponseEntity<?> searchUsers(@RequestParam String query) {
        List<User> users = userService.searchUsers(query);

        List<Map<String, Object>> response = users.stream()
                .map(user -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", user.getId());
                    map.put("username", user.getUsername());
                    map.put("createdAt", user.getCreatedAt());

                    int position = ratingService.getUserRatingPosition(user.getId());
                    map.put("rating", position);

                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}