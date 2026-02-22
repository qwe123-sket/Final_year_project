package com.example.final_year_project.controller;

import com.example.final_year_project.common.Result;
import com.example.final_year_project.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp");
    private static final long MAX_SIZE = 5 * 1024 * 1024; // 5MB

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @PostMapping("/image")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("Please select an image to upload");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException("Image size cannot exceed 5MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new BusinessException("Only JPG, PNG, GIF, WEBP images are allowed");
        }

        // 用 UUID 重命名防止重复
        String ext = getExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID().toString().replace("-", "") + ext;

        try {
            Path dir = Paths.get(uploadDir).toAbsolutePath();
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            Path target = dir.resolve(filename);
            file.transferTo(target.toFile());
        } catch (IOException e) {
            throw new BusinessException("Failed to save image: " + e.getMessage());
        }

        String url = "/api/uploads/" + filename;
        return Result.ok(Map.of("url", url));
    }

    private String getExtension(String filename) {
        if (filename == null) return ".jpg";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot).toLowerCase() : ".jpg";
    }
}
