// src/main/java/com/example/fishingapp/service/LikeService.java
package com.example.fishingapp.service;

import com.example.fishingapp.exception.ResourceNotFoundException;
import com.example.fishingapp.model.Catch;
import com.example.fishingapp.model.Like;
import com.example.fishingapp.model.User;
import com.example.fishingapp.repository.LikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LikeService {

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private CatchService catchService;

    @Autowired
    private UserService userService;

    @Autowired
    private RatingService ratingService;

    @Transactional
    public Map<String, Object> toggleLike(Long catchId, Long userId) {
        User user = userService.findById(userId);
        Catch catchEntity = catchService.getCatchById(catchId);

        Map<String, Object> response = new HashMap<>();

        if (likeRepository.existsByUserAndCatchEntity(user, catchEntity)) {
            Like like = likeRepository.findByUserAndCatchEntity(user, catchEntity)
                    .orElseThrow(() -> new ResourceNotFoundException("Like not found"));
            likeRepository.delete(like);
            catchEntity.decrementLikes();

            response.put("liked", false);
            response.put("likesCount", catchEntity.getLikesCount());
        } else {
            Like like = new Like();
            like.setUser(user);
            like.setCatchEntity(catchEntity);
            likeRepository.save(like);
            catchEntity.incrementLikes();

            response.put("liked", true);
            response.put("likesCount", catchEntity.getLikesCount());
        }

        catchService.saveCatch(catchEntity);

        // Автоматически пересчитываем рейтинг
        ratingService.recalculateUserRating(catchEntity.getUser().getId());

        return response;
    }

    public boolean isLiked(Long catchId, Long userId) {
        User user = userService.findById(userId);
        Catch catchEntity = catchService.getCatchById(catchId);
        return likeRepository.existsByUserAndCatchEntity(user, catchEntity);
    }

    public long getLikesCount(Long catchId) {
        Catch catchEntity = catchService.getCatchById(catchId);
        return likeRepository.countByCatchEntity(catchEntity);
    }

    public List<Map<String, Object>> getUserRatings() {
        List<Object[]> ratings = likeRepository.getUserRatings();

        return ratings.stream().map(row -> {
            Map<String, Object> map = new HashMap<>();
            map.put("userId", row[0]);
            map.put("likesCount", row[1]);
            return map;
        }).toList();
    }
}