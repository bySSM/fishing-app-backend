// src/main/java/com/example/fishingapp/service/RatingService.java
package com.example.fishingapp.service;

import com.example.fishingapp.dto.RatingResponse;
import com.example.fishingapp.model.User;
import com.example.fishingapp.repository.CatchRepository;
import com.example.fishingapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RatingService {

    @Autowired
    private CatchRepository catchRepository;

    @Autowired
    private UserRepository userRepository;

    public List<RatingResponse> getTop100ByMaxWeight() {
        List<Object[]> results = catchRepository.getRatingByMaxWeight();
        List<RatingResponse> responses = mapToRatingResponse(results);

        // Присваиваем позиции: 1, 2, 3...
        for (int i = 0; i < responses.size(); i++) {
            responses.get(i).setRating(i + 1);
        }

        return responses;
    }

    public List<RatingResponse> getTop100ByMaxWeightInArea(
            double centerLat, double centerLng, double radiusKm) {

        double latDelta = radiusKm / 111.0;
        double lngDelta = radiusKm / (111.0 * Math.cos(Math.toRadians(centerLat)));

        List<Object[]> results = catchRepository.getRatingByMaxWeightInArea(
                centerLat - latDelta, centerLat + latDelta,
                centerLng - lngDelta, centerLng + lngDelta
        );

        List<RatingResponse> responses = mapToRatingResponse(results);

        for (int i = 0; i < responses.size(); i++) {
            responses.get(i).setRating(i + 1);
        }

        return responses;
    }

    // Возвращает позицию пользователя в рейтинге (1, 2, 3...)
    public int getUserRatingPosition(Long userId) {
        List<RatingResponse> top100 = getTop100ByMaxWeight();

        for (RatingResponse response : top100) {
            if (response.getUserId().equals(userId)) {
                return response.getRating();
            }
        }

        return 0; // Не в топ-100
    }

    @Transactional
    public void recalculateUserRating(Long userId) {
        int position = getUserRatingPosition(userId);

        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setRating(position);
            userRepository.save(user);
            System.out.println("✅ Рейтинг обновлён: " + user.getUsername() + " → позиция #" + position);
        }
    }

    @Transactional
    public void recalculateAllUsersRating() {
        List<User> allUsers = userRepository.findAll();
        List<RatingResponse> top100 = getTop100ByMaxWeight();

        System.out.println("🔄 Пересчёт рейтинга для " + allUsers.size() + " пользователей...");

        for (User user : allUsers) {
            int position = 0;
            for (RatingResponse response : top100) {
                if (response.getUserId().equals(user.getId())) {
                    position = response.getRating();
                    break;
                }
            }

            user.setRating(position);
            userRepository.save(user);
            System.out.println("  ✅ " + user.getUsername() + " → позиция #" + position);
        }
    }

    private List<RatingResponse> mapToRatingResponse(List<Object[]> results) {
        return results.stream().map(row -> {
            RatingResponse response = new RatingResponse();
            response.setUserId(((Number) row[0]).longValue());
            response.setUsername((String) row[1]);
            response.setTotalWeight((Double) row[2]);
            response.setFishCount(((Number) row[3]).intValue());
            response.setMaxWeight((Double) row[4]);
            response.setLikesCount(((Number) row[5]).intValue());
            response.setRating(0); // Позиция будет присвоена позже

            return response;
        }).collect(Collectors.toList());
    }
}