// src/main/java/com/example/fishingapp/controller/CommentController.java
package com.example.fishingapp.controller;

import com.example.fishingapp.dto.CommentRequest;
import com.example.fishingapp.dto.CommentResponse;
import com.example.fishingapp.model.User;
import com.example.fishingapp.service.CommentService;
import com.example.fishingapp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserService userService;

    @PostMapping("/catch/{catchId}")
    public ResponseEntity<?> addComment(@PathVariable Long catchId,
                                        @Valid @RequestBody CommentRequest request) {
        String username = getCurrentUsername();
        User user = userService.findByUsername(username);

        CommentResponse comment = commentService.addComment(catchId, user.getId(), request);
        return ResponseEntity.ok(comment);
    }

    @GetMapping("/catch/{catchId}")
    public ResponseEntity<?> getCatchComments(@PathVariable Long catchId) {
        List<CommentResponse> comments = commentService.getCatchComments(catchId);
        long count = commentService.getCommentsCount(catchId);

        Map<String, Object> response = new HashMap<>();
        response.put("comments", comments);
        response.put("count", count);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyComments() {
        String username = getCurrentUsername();
        User user = userService.findByUsername(username);

        List<CommentResponse> comments = commentService.getUserComments(user.getId());
        return ResponseEntity.ok(comments);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId) {
        String username = getCurrentUsername();
        User user = userService.findByUsername(username);

        commentService.deleteComment(commentId, user.getId());
        return ResponseEntity.ok(Map.of("message", "Comment deleted successfully"));
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}