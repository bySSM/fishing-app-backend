// src/main/java/com/example/fishingapp/repository/CommentRepository.java
package com.example.fishingapp.repository;

import com.example.fishingapp.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByCatchEntityIdOrderByCreatedAtDesc(Long catchId);

    List<Comment> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByCatchEntityId(Long catchId);

    void deleteByCatchEntityId(Long catchId);
}