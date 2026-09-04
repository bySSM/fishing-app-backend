// src/main/java/com/example/fishingapp/service/FileStorageService.java
package com.example.fishingapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    // Разрешённые расширения. Всё остальное отбрасываем, даже если Content-Type "похож" на картинку.
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".webp");

    public String storeFile(MultipartFile file) {
        try {
            // Проверяем, что файл не пустой
            if (file.isEmpty()) {
                throw new RuntimeException("File is empty");
            }

            // Проверяем размер файла
            if (file.getSize() > 10 * 1024 * 1024) { // 10MB
                throw new RuntimeException("File too large (max 10MB)");
            }

            // Content-Type из запроса контролируется клиентом и не является надёжной проверкой,
            // но отсекаем совсем очевидный мусор на входе.
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new RuntimeException("Only images are allowed");
            }

            // Читаем байты один раз: они нужны и для проверки, что это реальная картинка,
            // и для сохранения на диск.
            byte[] fileBytes = file.getBytes();

            // Настоящая проверка: пытаемся декодировать файл как изображение.
            // Если это не картинка (например, HTML/SVG/скрипт с поддельным Content-Type),
            // ImageIO.read вернёт null или бросит исключение.
            BufferedImage image;
            try {
                image = ImageIO.read(new ByteArrayInputStream(fileBytes));
            } catch (IOException e) {
                throw new RuntimeException("Uploaded file is not a valid image");
            }
            if (image == null) {
                throw new RuntimeException("Uploaded file is not a valid image");
            }

            // Нормализуем имя файла и определяем расширение по белому списку,
            // а не по тому, что прислал клиент.
            String originalFilename = StringUtils.cleanPath(
                    file.getOriginalFilename() != null ? file.getOriginalFilename() : "");
            String extension = getFileExtension(originalFilename);
            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                extension = ".jpg";
            }

            // Создаём уникальное имя файла — имя клиента вообще не используется в итоговом пути.
            String newFilename = UUID.randomUUID().toString() + extension;

            // Создаём папку, если её нет
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            // Сохраняем файл
            Path targetLocation = uploadPath.resolve(newFilename);
            Files.write(targetLocation, fileBytes);

            // Возвращаем URL для доступа к файлу
            return "/uploads/" + newFilename;

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    public void deleteFile(String fileUrl) {
        try {
            if (fileUrl == null || !fileUrl.startsWith("/uploads/")) {
                return;
            }

            String filename = fileUrl.substring("/uploads/".length());

            // Запрещаем любые сегменты пути и разделители — имя должно быть простым именем файла,
            // как мы сами его генерируем в storeFile (UUID + расширение).
            if (filename.isEmpty()
                    || filename.contains("..")
                    || filename.contains("/")
                    || filename.contains("\\")) {
                System.err.println("Suspicious file path rejected on delete: " + fileUrl);
                return;
            }

            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path filePath = uploadPath.resolve(filename).normalize();

            // Доп. защита: даже после фильтра выше проверяем, что итоговый путь
            // остаётся внутри папки загрузок.
            if (!filePath.startsWith(uploadPath)) {
                System.err.println("Path traversal attempt blocked on delete: " + fileUrl);
                return;
            }

            Files.deleteIfExists(filePath);
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