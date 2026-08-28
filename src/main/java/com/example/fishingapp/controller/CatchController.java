// src/main/java/com/example/fishingapp/controller/CatchController.java
package com.example.fishingapp.controller;

import com.example.fishingapp.dto.CatchRequest;
import com.example.fishingapp.dto.CatchResponse;
import com.example.fishingapp.model.Catch;
import com.example.fishingapp.model.User;
import com.example.fishingapp.service.CatchService;
import com.example.fishingapp.service.FileStorageService;
import com.example.fishingapp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/catches")
public class CatchController {

    @Autowired
    private CatchService catchService;

    @Autowired
    private UserService userService;

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping
    public ResponseEntity<?> createCatch(@Valid @RequestBody CatchRequest request) {
        try {
            String username = getCurrentUsername();
            User user = userService.findByUsername(username);

            Catch newCatch = catchService.createCatch(request, user.getId());
            return ResponseEntity.ok(catchService.mapToResponse(newCatch));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // src/main/java/com/example/fishingapp/controller/CatchController.java
// Обнови метод createCatchWithPhoto:

    @PostMapping("/with-photo")
    public ResponseEntity<?> createCatchWithPhoto(
            @RequestParam("photo") MultipartFile photo,
            @RequestParam("fishType") String fishType,
            @RequestParam(value = "weight", required = false) Double weight,
            @RequestParam(value = "length", required = false) Double length,
            @RequestParam("latitude") Double latitude,
            @RequestParam("longitude") Double longitude,
            @RequestParam(value = "bait", required = false) String bait,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "isLocationHidden", required = false, defaultValue = "false") Boolean isLocationHidden) {

        try {
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
            request.setIsLocationHidden(isLocationHidden); // <-- ВАЖНО!

            Catch newCatch = catchService.createCatch(request, user.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("catch", catchService.mapToResponse(newCatch));
            response.put("photoUrl", photoUrl);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{catchId}/photo")
    public ResponseEntity<?> uploadPhotoForCatch(@PathVariable Long catchId,
                                                 @RequestParam("photo") MultipartFile photo) {
        try {
            String username = getCurrentUsername();
            User user = userService.findByUsername(username);

            Catch existingCatch = catchService.getCatchById(catchId);

            // Проверяем, что улов принадлежит пользователю
            if (!existingCatch.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body("You can only add photos to your own catches");
            }

            // Удаляем старое фото, если есть
            if (existingCatch.getPhotoUrl() != null) {
                fileStorageService.deleteFile(existingCatch.getPhotoUrl());
            }

            // Сохраняем новое фото
            String photoUrl = fileStorageService.storeFile(photo);
            existingCatch.setPhotoUrl(photoUrl);

            Catch updatedCatch = catchService.saveCatch(existingCatch);

            return ResponseEntity.ok(Map.of(
                    "catch", catchService.mapToResponse(updatedCatch),
                    "photoUrl", photoUrl
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
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
    public List<CatchResponse> getNearbyCatches(@RequestParam double lat,
                                                @RequestParam double lng,
                                                @RequestParam(defaultValue = "10") double radiusKm) {
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

    // В CatchController.java добавь:

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCatch(@PathVariable Long id,
                                         @Valid @RequestBody CatchRequest request) {
        try {
            String username = getCurrentUsername();
            User user = userService.findByUsername(username);

            Catch existingCatch = catchService.getCatchById(id);

            // Проверяем, что улов принадлежит пользователю
            if (!existingCatch.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body("You can only edit your own catches");
            }

            Catch updatedCatch = catchService.updateCatch(id, request);
            return ResponseEntity.ok(catchService.mapToResponse(updatedCatch));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCatch(@PathVariable Long id) {
        try {
            String username = getCurrentUsername();
            User user = userService.findByUsername(username);

            Catch existingCatch = catchService.getCatchById(id);

            // Проверяем, что улов принадлежит пользователю
            if (!existingCatch.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body("You can only delete your own catches");
            }

            // Удаляем фото, если есть
            if (existingCatch.getPhotoUrl() != null) {
                fileStorageService.deleteFile(existingCatch.getPhotoUrl());
            }

            catchService.deleteCatch(id);
            return ResponseEntity.ok(Map.of("message", "Catch deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}