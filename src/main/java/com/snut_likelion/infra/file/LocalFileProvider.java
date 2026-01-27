package com.snut_likelion.infra.file;

import com.snut_likelion.global.error.exception.BadRequestException;
import com.snut_likelion.global.provider.FileProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Component
@Profile("dev")
public class LocalFileProvider implements FileProvider {

    @Value("${server.url}")
    private String serverUrl;

    private final Path rootLocation;

    public LocalFileProvider(@Value("${file.upload-dir}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.rootLocation);
        } catch (IOException e) {
            throw new IllegalStateException("파일 저장 디렉토리 생성 실패: " + rootLocation, e);
        }
    }

    @Override
    public Resource getFile(String storedFileName) {
        try {
            Path file = rootLocation.resolve(storedFileName).normalize();

            // rootLocation 밖으로 탈출하면 차단
            if (!file.startsWith(rootLocation)) {
                throw new BadRequestException(FileErrorCode.INVALID_FILE_KEY);
            }

            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new BadRequestException(FileErrorCode.FILE_NOT_FOUND);

        } catch (MalformedURLException ex) {
            throw new BadRequestException(FileErrorCode.BAD_FILE_URL, ex.getMessage());
        }
    }

    @Override
    public void deleteFile(String storedFileName) {
        try {
            Path file = rootLocation.resolve(storedFileName).normalize();

            // rootLocation 밖이면 차단
            if (!file.startsWith(rootLocation)) {
                throw new BadRequestException(FileErrorCode.INVALID_FILE_KEY);
            }

            Files.deleteIfExists(file);
        } catch (IOException ex) {
            log.warn("파일 삭제 실패: {}", storedFileName, ex);
        }
    }

    @Override
    public String storeFile(MultipartFile file) {
        // 원본 파일명에서 확장자 추출
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // UUID 및 타임스탬프로 충돌 방지, 확장자 포함
        String filename = System.currentTimeMillis()
                + "-" + UUID.randomUUID()
                + extension;

        try (InputStream input = file.getInputStream()) {
            Path destination = rootLocation.resolve(filename);
            Files.copy(input, destination, StandardCopyOption.REPLACE_EXISTING);
            return filename;
        } catch (IOException ex) {
            throw new IllegalStateException("파일 저장 실패: " + filename, ex);
        }
    }

    @Override
    public String extractImageName(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            throw new BadRequestException(FileErrorCode.INVALID_IMAGE_URL);
        }
        String[] parts = imageUrl.split("imageName=");
        if (parts.length == 0) {
            throw new BadRequestException(FileErrorCode.INVALID_IMAGE_URL);
        }

        return parts[parts.length - 1];
    }

    @Override
    public String buildImageUrl(String storedFileName) {
        return String.format("%s/api/v1/images?imageName=%s", serverUrl, storedFileName);
    }

    private void validateStoredFileName(String storedFileName) {
        if (storedFileName == null || storedFileName.isBlank()) {
            throw new BadRequestException(FileErrorCode.INVALID_FILE_KEY);
        }

        // key 규칙: images/ 로 시작 + '..' 금지
        if (!storedFileName.startsWith("images/") || storedFileName.contains("..")) {
            throw new BadRequestException(FileErrorCode.INVALID_FILE_KEY);
        }

        // 윈도우 경로 우회 방지
        if (storedFileName.contains("\\")) {
            throw new BadRequestException(FileErrorCode.INVALID_FILE_KEY);
        }
    }

}
