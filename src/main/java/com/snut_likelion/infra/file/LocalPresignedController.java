package com.snut_likelion.infra.file;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Slf4j
@RestController
@RequestMapping("/api/v1/files/local")
@Profile("dev")
@Hidden
public class LocalPresignedController {  // Dev용 가짜 S3

    private final Path rootLocation;

    public LocalPresignedController(@Value("${file.upload-dir}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @PutMapping("/presigned-put")
    public ResponseEntity<Void> mockPutUpload(
            @RequestParam("key") String key,
            HttpServletRequest request
    ) {
        try {
            // 1. 보안 검사: key는 반드시 images/로 시작해야 함 & 상위폴더 이동(..) 금지
            if (key == null || key.isBlank() || !key.startsWith("images/") || key.contains("..")) {
                log.warn("[Local S3] Invalid key: {}", key);
                return ResponseEntity.badRequest().build();
            }

            // 2. 경로 계산 및 탈출 방지 (한 번 더 체크)
            Path targetPath = rootLocation.resolve(key).normalize();
            if (!targetPath.startsWith(rootLocation)) {
                log.warn("[Local S3] Path traversal attempt: {}", targetPath);
                return ResponseEntity.badRequest().build();
            }

            // 3. 저장 (폴더 없으면 생성)
            Files.createDirectories(targetPath.getParent());
            Files.copy(request.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            log.info("[Local S3] File uploaded to: {}", targetPath);
            return ResponseEntity.ok().build();

        } catch (IOException e) {
            log.error("[Local S3] Upload failed", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}