// src/main/java/com/example/fishingapp/repository/CatchRepository.java
package com.example.fishingapp.repository;

import com.example.fishingapp.model.Catch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface CatchRepository extends JpaRepository<Catch, Long> {

    List<Catch> findByUserId(Long userId);

    @Query("SELECT c FROM Catch c WHERE " +
            "(6371 * acos(cos(radians(:lat)) * cos(radians(c.latitude)) * " +
            "cos(radians(c.longitude) - radians(:lng)) + " +
            "sin(radians(:lat)) * sin(radians(c.latitude)))) < :radius " +
            "AND c.isLocationHidden = false")
    List<Catch> findNearbyCatches(@Param("lat") double latitude,
                                  @Param("lng") double longitude,
                                  @Param("radius") double radiusKm);

    @Query("SELECT c FROM Catch c WHERE c.user.id = :userId ORDER BY c.weight DESC NULLS LAST")
    List<Catch> findTopFishesByUserId(@Param("userId") Long userId,
                                      org.springframework.data.domain.Pageable pageable);

    // Рейтинг: топ-15 самых тяжёлых рыб каждого пользователя
    @Query(value = "SELECT user_id, username, " +
            "SUM(weight) as total_weight, " +
            "COUNT(*) as fish_count, " +
            "MAX(weight) as max_weight, " +
            "0 as likes_count " +
            "FROM (" +
            "  SELECT c.user_id as user_id, u.username as username, c.weight as weight, " +
            "         ROW_NUMBER() OVER (PARTITION BY c.user_id ORDER BY c.weight DESC NULLS LAST) as rn " +
            "  FROM catches c " +
            "  JOIN users u ON c.user_id = u.id " +
            "  WHERE c.weight IS NOT NULL" +
            ") sub " +
            "WHERE rn <= 15 " +
            "GROUP BY user_id, username " +
            "ORDER BY total_weight DESC " +
            "LIMIT 100",
            nativeQuery = true)
    List<Object[]> getRatingByMaxWeight();

    // Рейтинг в области: топ-15 рыб пользователей в заданной зоне
    @Query(value = "SELECT user_id, username, " +
            "SUM(weight) as total_weight, " +
            "COUNT(*) as fish_count, " +
            "MAX(weight) as max_weight, " +
            "0 as likes_count " +
            "FROM (" +
            "  SELECT c.user_id as user_id, u.username as username, c.weight as weight, " +
            "         ROW_NUMBER() OVER (PARTITION BY c.user_id ORDER BY c.weight DESC NULLS LAST) as rn " +
            "  FROM catches c " +
            "  JOIN users u ON c.user_id = u.id " +
            "  WHERE c.weight IS NOT NULL " +
            "    AND c.is_location_hidden = false " +
            "    AND c.latitude BETWEEN :minLat AND :maxLat " +
            "    AND c.longitude BETWEEN :minLng AND :maxLng" +
            ") sub " +
            "WHERE rn <= 15 " +
            "GROUP BY user_id, username " +
            "ORDER BY total_weight DESC " +
            "LIMIT 100",
            nativeQuery = true)
    List<Object[]> getRatingByMaxWeightInArea(
            @Param("minLat") double minLat,
            @Param("maxLat") double maxLat,
            @Param("minLng") double minLng,
            @Param("maxLng") double maxLng);
}