package com.snut_likelion.infra.file;

import com.snut_likelion.global.provider.UploadedFileVerifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Component
@Profile("dev")  // Dev 구현체 (Local)
public class LocalUploadedFileVerifier implements UploadedFileVerifier {

    private final Path rootLocation;

    public LocalUploadedFileVerifier(@Value("${file.upload-dir}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @Override
    public boolean exists(String storedFileName) {
        try {
            Path target = rootLocation.resolve(storedFileName).normalize();
            if (!target.startsWith(rootLocation)) {
                // path traversal 방지
                return false;
            }
            return Files.exists(target) && Files.isRegularFile(target);
        } catch (Exception e) {
            log.warn("[LocalUploadedFileVerifier] exists check failed: {}", storedFileName, e);
            return false;
        }
    }
}
