// src/main/java/com/example/fishingapp/service/FileStorageService.java
package com.example.fishingapp.service;

import com.example.fishingapp.exception.ValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    private static final int MAX_DECODE_DIMENSION_PX = 8000;
    private static final int MAX_STORED_DIMENSION_PX = 1920;
    private static final float JPEG_QUALITY = 0.85f;

    public String storeFile(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new ValidationException("File is empty");
            }

            if (file.getSize() > 10 * 1024 * 1024) {
                throw new ValidationException("File too large (max 10MB)");
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new ValidationException("Only images are allowed");
            }

            byte[] fileBytes = file.getBytes();

            checkDeclaredDimensions(fileBytes);

            BufferedImage image;
            try {
                image = ImageIO.read(new ByteArrayInputStream(fileBytes));
            } catch (IOException e) {
                throw new ValidationException("Uploaded file is not a valid image");
            }
            if (image == null) {
                throw new ValidationException("Uploaded file is not a valid image");
            }

            byte[] finalBytes = resizeAndCompress(image);

            String newFilename = UUID.randomUUID().toString() + ".jpg";

            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            Path targetLocation = uploadPath.resolve(newFilename);
            Files.write(targetLocation, finalBytes);

            return "/uploads/" + newFilename;

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    private void checkDeclaredDimensions(byte[] fileBytes) throws IOException {
        try (ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(fileBytes))) {
            if (iis == null) {
                throw new ValidationException("Uploaded file is not a valid image");
            }

            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (!readers.hasNext()) {
                throw new ValidationException("Uploaded file is not a valid image");
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(iis, true, true);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);

                if (width > MAX_DECODE_DIMENSION_PX || height > MAX_DECODE_DIMENSION_PX) {
                    throw new ValidationException(
                            "Image dimensions are too large (max " + MAX_DECODE_DIMENSION_PX + "px)");
                }
            } finally {
                reader.dispose();
            }
        }
    }

    private byte[] resizeAndCompress(BufferedImage image) throws IOException {
        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage toEncode = image;

        if (width > MAX_STORED_DIMENSION_PX || height > MAX_STORED_DIMENSION_PX) {
            double scale = Math.min(
                    (double) MAX_STORED_DIMENSION_PX / width,
                    (double) MAX_STORED_DIMENSION_PX / height
            );
            int newWidth = Math.max(1, (int) Math.round(width * scale));
            int newHeight = Math.max(1, (int) Math.round(height * scale));

            BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = resized.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, newWidth, newHeight);
            g.drawImage(image, 0, 0, newWidth, newHeight, null);
            g.dispose();
            toEncode = resized;
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        writeJpegWithQuality(toEncode, output, JPEG_QUALITY);
        return output.toByteArray();
    }

    private void writeJpegWithQuality(BufferedImage image, ByteArrayOutputStream output, float quality)
            throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        if (!writers.hasNext()) {
            ImageIO.write(image, "jpg", output);
            return;
        }

        ImageWriter writer = writers.next();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(output)) {
            writer.setOutput(ios);

            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);

            writer.write(null, new IIOImage(image, null, null), param);
        } finally {
            writer.dispose();
        }
    }

    public void deleteFile(String fileUrl) {
        try {
            if (fileUrl == null || !fileUrl.startsWith("/uploads/")) {
                return;
            }

            String filename = fileUrl.substring("/uploads/".length());

            if (filename.isEmpty()
                    || filename.contains("..")
                    || filename.contains("/")
                    || filename.contains("\\")) {
                System.err.println("Suspicious file path rejected on delete: " + fileUrl);
                return;
            }

            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path filePath = uploadPath.resolve(filename).normalize();

            if (!filePath.startsWith(uploadPath)) {
                System.err.println("Path traversal attempt blocked on delete: " + fileUrl);
                return;
            }

            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("Failed to delete file: " + e.getMessage());
        }
    }
}