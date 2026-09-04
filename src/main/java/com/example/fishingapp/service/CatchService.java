// src/main/java/com/example/fishingapp/service/CatchService.java
package com.example.fishingapp.service;

import com.example.fishingapp.dto.CatchRequest;
import com.example.fishingapp.dto.CatchResponse;
import com.example.fishingapp.exception.ResourceNotFoundException;
import com.example.fishingapp.model.Catch;
import com.example.fishingapp.model.User;
import com.example.fishingapp.repository.CatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CatchService {

    @Autowired
    private CatchRepository catchRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private RatingService ratingService;

    public Catch createCatch(CatchRequest request, Long userId) {
        User user = userService.findById(userId);

        Catch newCatch = new Catch();
        newCatch.setFishType(request.getFishType());
        newCatch.setWeight(request.getWeight());
        newCatch.setLength(request.getLength());
        newCatch.setLatitude(request.getLatitude());
        newCatch.setLongitude(request.getLongitude());
        newCatch.setBait(request.getBait());
        newCatch.setDescription(request.getDescription());
        newCatch.setPhotoUrl(request.getPhotoUrl());
        newCatch.setIsLocationHidden(request.getIsLocationHidden() != null ? request.getIsLocationHidden() : false);
        newCatch.setUser(user);

        Catch savedCatch = catchRepository.save(newCatch);

        // Автоматически пересчитываем рейтинг
        ratingService.recalculateUserRating(userId);

        return savedCatch;
    }

    public List<Catch> getUserCatches(Long userId) {
        return catchRepository.findByUserId(userId);
    }

    public List<Catch> getNearbyCatches(double lat, double lng, double radiusKm) {
        return catchRepository.findNearbyCatches(lat, lng, radiusKm);
    }

    public Catch getCatchById(Long id) {
        return catchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Catch not found"));
    }

    public Catch saveCatch(Catch catchEntity) {
        Catch savedCatch = catchRepository.save(catchEntity);
        if (catchEntity.getUser() != null) {
            ratingService.recalculateUserRating(catchEntity.getUser().getId());
        }
        return savedCatch;
    }

    public Catch updateCatch(Long id, CatchRequest request) {
        Catch existingCatch = getCatchById(id);

        existingCatch.setFishType(request.getFishType());
        existingCatch.setWeight(request.getWeight());
        existingCatch.setLength(request.getLength());
        existingCatch.setLatitude(request.getLatitude());
        existingCatch.setLongitude(request.getLongitude());
        existingCatch.setBait(request.getBait());
        existingCatch.setDescription(request.getDescription());

        // ВНИМАНИЕ: photoUrl намеренно НЕ обновляется здесь, даже если он передан в запросе.
        if (request.getIsLocationHidden() != null) {
            existingCatch.setIsLocationHidden(request.getIsLocationHidden());
        }

        Catch savedCatch = catchRepository.save(existingCatch);

        // Автоматически пересчитываем рейтинг
        ratingService.recalculateUserRating(savedCatch.getUser().getId());

        return savedCatch;
    }

    public void deleteCatch(Long id) {
        Catch catchEntity = getCatchById(id);
        Long userId = catchEntity.getUser().getId();

        catchRepository.deleteById(id);

        // Автоматически пересчитываем рейтинг
        ratingService.recalculateUserRating(userId);
    }

    public List<CatchResponse> getTopFishes(Long userId, int limit) {
        return catchRepository.findTopFishesByUserId(userId, PageRequest.of(0, limit))
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public CatchResponse mapToResponse(Catch catchEntity) {
        CatchResponse response = new CatchResponse();
        response.setId(catchEntity.getId());
        response.setFishType(catchEntity.getFishType());
        response.setWeight(catchEntity.getWeight());
        response.setLength(catchEntity.getLength());

        if (catchEntity.getIsLocationHidden() != null && catchEntity.getIsLocationHidden()) {
            response.setLatitude(null);
            response.setLongitude(null);
        } else {
            response.setLatitude(catchEntity.getLatitude());
            response.setLongitude(catchEntity.getLongitude());
        }

        response.setBait(catchEntity.getBait());
        response.setDescription(catchEntity.getDescription());
        response.setPhotoUrl(catchEntity.getPhotoUrl());
        response.setCreatedAt(catchEntity.getCreatedAt());
        response.setLikesCount(catchEntity.getLikesCount());
        response.setIsLocationHidden(catchEntity.getIsLocationHidden());

        if (catchEntity.getUser() != null) {
            response.setUserId(catchEntity.getUser().getId());
            response.setUsername(catchEntity.getUser().getUsername());
        }

        return response;
    }
}