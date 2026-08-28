// src/main/java/com/example/fishingapp/dto/CommentResponse.java
package com.example.fishingapp.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentResponse {
    private Long id;
    private String content;
    private Long userId;
    private String username;
    private Long catchId;
    private LocalDateTime createdAt;
}