// src/main/java/com/example/fishingapp/repository/LikeRepository.java
package com.example.fishingapp.repository;

import com.example.fishingapp.model.Like;
import com.example.fishingapp.model.Catch;
import com.example.fishingapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {

    Optional<Like> findByUserAndCatchEntity(User user, Catch catchEntity);

    boolean existsByUserAndCatchEntity(User user, Catch catchEntity);

    long countByCatchEntity(Catch catchEntity);

    List<Like> findByCatchEntity(Catch catchEntity);

    @Query("SELECT l.catchEntity.user.id, COUNT(l) as likeCount " +
            "FROM Like l " +
            "GROUP BY l.catchEntity.user.id " +
            "ORDER BY likeCount DESC")
    List<Object[]> getUserRatings();

    @Query("SELECT l.catchEntity.user, COUNT(l) as likeCount " +
            "FROM Like l " +
            "WHERE l.catchEntity.user.id = :userId " +
            "GROUP BY l.catchEntity.user")
    Object[] getUserRating(@Param("userId") Long userId);
}