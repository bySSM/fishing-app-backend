// src/main/java/com/example/fishingapp/service/FileStorageService.java
package com.example.fishingapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    public String storeFile(MultipartFile file) {
        try {
            // Нормализуем имя файла
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

            // Проверяем, что файл не пустой
            if (file.isEmpty()) {
                throw new RuntimeException("File is empty");
            }

            // Проверяем размер файла
            if (file.getSize() > 10 * 1024 * 1024) { // 10MB
                throw new RuntimeException("File too large (max 10MB)");
            }

            // Проверяем тип файла
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new RuntimeException("Only images are allowed");
            }

            // Создаём уникальное имя файла
            String extension = getFileExtension(originalFilename);
            String newFilename = UUID.randomUUID().toString() + extension;

            // Создаём папку, если её нет
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            // Сохраняем файл
            Path targetLocation = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Возвращаем URL для доступа к файлу
            return "/uploads/" + newFilename;

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    public void deleteFile(String fileUrl) {
        try {
            if (fileUrl != null && fileUrl.startsWith("/uploads/")) {
                String filename = fileUrl.substring("/uploads/".length());
                Path filePath = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(filename);
                Files.deleteIfExists(filePath);
            }
        } catch (IOException e) {
            // Логируем, но не бросаем ошибку
            System.err.println("Failed to delete file: " + e.getMessage());
        }
    }

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex).toLowerCase();
        }
        return ".jpg"; // По умолчанию
    }
}