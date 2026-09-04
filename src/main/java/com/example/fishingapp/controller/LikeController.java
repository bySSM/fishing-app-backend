// src/main/java/com/example/fishingapp/controller/LikeController.java
package com.example.fishingapp.controller;

import com.example.fishingapp.model.User;
import com.example.fishingapp.service.LikeService;
import com.example.fishingapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/likes")
public class LikeController {

    @Autowired
    private LikeService likeService;

    @Autowired
    private UserService userService;

    @PostMapping("/catch/{catchId}/toggle")
    public ResponseEntity<?> toggleLike(@PathVariable Long catchId) {
        String username = getCurrentUsername();
        User user = userService.findByUsername(username);

        Map<String, Object> result = likeService.toggleLike(catchId, user.getId());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/catch/{catchId}/status")
    public ResponseEntity<?> getLikeStatus(@PathVariable Long catchId) {
        String username = getCurrentUsername();
        User user = userService.findByUsername(username);

        boolean liked = likeService.isLiked(catchId, user.getId());
        long count = likeService.getLikesCount(catchId);

        return ResponseEntity.ok(Map.of(
                "liked", liked,
                "likesCount", count
        ));
    }

    @GetMapping("/ratings")
    public List<Map<String, Object>> getUserRatings() {
        return likeService.getUserRatings();
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}