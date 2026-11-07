package com.lms.lms.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils; // تم الإضافة للتنظيف
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileUploadService {
    private final String uploadDir = new File("uploads").getAbsolutePath();

    public String uploadFile(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // تنظيف اسم الملف لسد ثغرة الـ Path Traversal
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        if(originalFileName.contains("..")) {
            throw new RuntimeException("Sorry! Filename contains invalid path sequence " + originalFileName);
        }

        String fileName = UUID.randomUUID() + "_" + originalFileName;
        Path filePath = uploadPath.resolve(fileName);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return "/uploads/" + fileName;
    }
}