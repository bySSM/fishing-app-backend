// src/main/java/com/example/fishingapp/controller/CatchController.java
package com.example.fishingapp.controller;

import com.example.fishingapp.dto.CatchRequest;
import com.example.fishingapp.dto.CatchResponse;
import com.example.fishingapp.exception.ForbiddenOperationException;
import com.example.fishingapp.model.Catch;
import com.example.fishingapp.model.User;
import com.example.fishingapp.service.CatchService;
import com.example.fishingapp.service.FileStorageService;
import com.example.fishingapp.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/catches")
@Validated
public class CatchController {

    @Autowired
    private CatchService catchService;

    @Autowired
    private UserService userService;

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping
    public ResponseEntity<?> createCatch(@Valid @RequestBody CatchRequest request) {
        String username = getCurrentUsername();
        User user = userService.findByUsername(username);

        request.setPhotoUrl(null);

        Catch newCatch = catchService.createCatch(request, user.getId());
        return ResponseEntity.ok(catchService.mapToResponse(newCatch));
    }

    @PostMapping("/with-photo")
    public ResponseEntity<?> createCatchWithPhoto(
            @RequestParam("photo") MultipartFile photo,
            @RequestParam("fishType") String fishType,

            @RequestParam(value = "weight", required = false)
            @DecimalMin(value = "0.01", message = "Weight must be greater than 0")
            @DecimalMax(value = "1000", message = "Weight seems unrealistic (max 1000 kg)")
            Double weight,

            @RequestParam(value = "length", required = false)
            @DecimalMin(value = "0.1", message = "Length must be greater than 0")
            @DecimalMax(value = "500", message = "Length seems unrealistic (max 500 cm)")
            Double length,

            @RequestParam("latitude")
            @DecimalMin(value = "-90", message = "Latitude must be between -90 and 90")
            @DecimalMax(value = "90", message = "Latitude must be between -90 and 90")
            Double latitude,

            @RequestParam("longitude")
            @DecimalMin(value = "-180", message = "Longitude must be between -180 and 180")
            @DecimalMax(value = "180", message = "Longitude must be between -180 and 180")
            Double longitude,

            @RequestParam(value = "bait", required = false) String bait,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "isLocationHidden", required = false, defaultValue = "false") Boolean isLocationHidden) {

        String username = getCurrentUsername();
        User user = userService.findByUsername(username);

        String photoUrl = fileStorageService.storeFile(photo);

        CatchRequest request = new CatchRequest();
        request.setFishType(fishType);
        request.setWeight(weight);
        request.setLength(length);
        request.setLatitude(latitude);
        request.setLongitude(longitude);
        request.setBait(bait);
        request.setDescription(description);
        request.setPhotoUrl(photoUrl);
        request.setIsLocationHidden(isLocationHidden);

        Catch newCatch = catchService.createCatch(request, user.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("catch", catchService.mapToResponse(newCatch));
        response.put("photoUrl", photoUrl);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{catchId}/photo")
    public ResponseEntity<?> uploadPhotoForCatch(@PathVariable Long catchId,
                                                 @RequestParam("photo") MultipartFile photo) {
        String username = getCurrentUsername();
        User user = userService.findByUsername(username);

        Catch existingCatch = catchService.getCatchById(catchId);

        if (!existingCatch.getUser().getId().equals(user.getId())) {
            throw new ForbiddenOperationException("You can only add photos to your own catches");
        }

        if (existingCatch.getPhotoUrl() != null) {
            fileStorageService.deleteFile(existingCatch.getPhotoUrl());
        }

        String photoUrl = fileStorageService.storeFile(photo);
        existingCatch.setPhotoUrl(photoUrl);

        Catch updatedCatch = catchService.saveCatch(existingCatch);

        return ResponseEntity.ok(Map.of(
                "catch", catchService.mapToResponse(updatedCatch),
                "photoUrl", photoUrl
        ));
    }

    @GetMapping("/my")
    public List<CatchResponse> getMyCatches() {
        String username = getCurrentUsername();
        User user = userService.findByUsername(username);
        return catchService.getUserCatches(user.getId())
                .stream()
                .map(catchService::mapToResponse)
                .toList();
    }

    @GetMapping("/user/{userId}")
    public List<CatchResponse> getUserCatches(@PathVariable Long userId) {
        return catchService.getUserCatches(userId)
                .stream()
                .map(catchService::mapToResponse)
                .toList();
    }

    @GetMapping("/nearby")
    public List<CatchResponse> getNearbyCatches(
            @RequestParam
            @DecimalMin(value = "-90", message = "Latitude must be between -90 and 90")
            @DecimalMax(value = "90", message = "Latitude must be between -90 and 90")
            double lat,

            @RequestParam
            @DecimalMin(value = "-180", message = "Longitude must be between -180 and 180")
            @DecimalMax(value = "180", message = "Longitude must be between -180 and 180")
            double lng,

            @RequestParam(defaultValue = "10")
            @DecimalMin(value = "0.1", message = "radiusKm must be greater than 0")
            @DecimalMax(value = "20000", message = "radiusKm must not exceed 20000 (whole Earth)")
            double radiusKm) {

        return catchService.getNearbyCatches(lat, lng, radiusKm)
                .stream()
                .map(catchService::mapToResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public CatchResponse getCatchById(@PathVariable Long id) {
        return catchService.mapToResponse(catchService.getCatchById(id));
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCatch(@PathVariable Long id,
                                         @Valid @RequestBody CatchRequest request) {
        String username = getCurrentUsername();
        User user = userService.findByUsername(username);

        Catch existingCatch = catchService.getCatchById(id);

        if (!existingCatch.getUser().getId().equals(user.getId())) {
            throw new ForbiddenOperationException("You can only edit your own catches");
        }

        request.setPhotoUrl(null);

        Catch updatedCatch = catchService.updateCatch(id, request);
        return ResponseEntity.ok(catchService.mapToResponse(updatedCatch));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCatch(@PathVariable Long id) {
        String username = getCurrentUsername();
        User user = userService.findByUsername(username);

        Catch existingCatch = catchService.getCatchById(id);

        if (!existingCatch.getUser().getId().equals(user.getId())) {
            throw new ForbiddenOperationException("You can only delete your own catches");
        }

        if (existingCatch.getPhotoUrl() != null) {
            fileStorageService.deleteFile(existingCatch.getPhotoUrl());
        }

        catchService.deleteCatch(id);
        return ResponseEntity.ok(Map.of("message", "Catch deleted successfully"));
    }
}