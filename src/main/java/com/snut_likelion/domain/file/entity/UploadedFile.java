package com.snut_likelion.domain.file.entity;

import com.snut_likelion.domain.file.dto.UploadCategory;
import com.snut_likelion.global.support.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@Table(
        name = "uploaded_file",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_uploaded_file_stored_name", columnNames = "storedFileName")
        }
)
@NoArgsConstructor(access = PROTECTED)
public class UploadedFile extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UploadCategory uploadCategory;

    @Column(nullable = false, length = 500)
    private String storedFileName; // S3 key (images/..)

    @Column(nullable = false, length = 255)
    private String originalFileName;

    @Column(nullable = false, length = 100)
    private String contentType;

    @Column(nullable = false)
    private Long contentLength;

    @Builder
    private UploadedFile(
            UploadCategory uploadCategory,
            String storedFileName,
            String originalFileName,
            String contentType,
            Long contentLength
    ) {
        this.uploadCategory = uploadCategory;
        this.storedFileName = storedFileName;
        this.originalFileName = originalFileName;
        this.contentType = contentType;
        this.contentLength = contentLength;
    }
}
