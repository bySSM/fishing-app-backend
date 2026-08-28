// src/main/java/com/example/fishingapp/service/CommentService.java
package com.example.fishingapp.service;

import com.example.fishingapp.dto.CommentRequest;
import com.example.fishingapp.dto.CommentResponse;
import com.example.fishingapp.model.Catch;
import com.example.fishingapp.model.Comment;
import com.example.fishingapp.model.User;
import com.example.fishingapp.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CatchService catchService;

    @Autowired
    private UserService userService;

    @Transactional
    public CommentResponse addComment(Long catchId, Long userId, CommentRequest request) {
        User user = userService.findById(userId);
        Catch catchEntity = catchService.getCatchById(catchId);

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setUser(user);
        comment.setCatchEntity(catchEntity);

        Comment saved = commentRepository.save(comment);
        return mapToResponse(saved);
    }

    public List<CommentResponse> getCatchComments(Long catchId) {
        List<Comment> comments = commentRepository.findByCatchEntityIdOrderByCreatedAtDesc(catchId);
        return comments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<CommentResponse> getUserComments(Long userId) {
        List<Comment> comments = commentRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return comments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public long getCommentsCount(Long catchId) {
        return commentRepository.countByCatchEntityId(catchId);
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getUser().getId().equals(userId)) {
            throw new RuntimeException("You can only delete your own comments");
        }

        commentRepository.delete(comment);
    }

    @Transactional
    public void deleteAllCatchComments(Long catchId) {
        commentRepository.deleteByCatchEntityId(catchId);
    }

    private CommentResponse mapToResponse(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setUserId(comment.getUser().getId());
        response.setUsername(comment.getUser().getUsername());
        response.setCatchId(comment.getCatchEntity().getId());
        response.setCreatedAt(comment.getCreatedAt());
        return response;
    }
}