package com.snut_likelion.domain.file.repository;

import com.snut_likelion.domain.file.entity.UploadedFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UploadedFileRepository extends JpaRepository<UploadedFile, Long> {
    Optional<UploadedFile> findByStoredFileName(String storedFileName);
}
