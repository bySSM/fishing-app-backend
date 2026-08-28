// src/main/java/com/example/fishingapp/repository/UserRepository.java
package com.example.fishingapp.repository;

import com.example.fishingapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<User> searchByUsername(@Param("query") String query);

    // UserRepository.java
    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.rating = :rating WHERE u.id = :userId")
    void updateRating(@Param("userId") Long userId, @Param("rating") Integer rating);
}