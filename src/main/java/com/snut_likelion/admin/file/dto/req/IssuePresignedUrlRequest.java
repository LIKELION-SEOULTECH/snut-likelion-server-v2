package com.snut_likelion.admin.file.dto.req;

import com.snut_likelion.domain.file.dto.FileStorageType;
import com.snut_likelion.domain.file.dto.UploadCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
@Schema(description = "Presigned URL 발급 요청")
public class IssuePresignedUrlRequest {

    @Schema(description = "원본 파일명 (확장자 포함)", example = "my-profile.png")
    @NotBlank(message = "원본 파일명은 필수입니다.")
    private String originalFileName;

    @Schema(
            description = "파일의 Content-Type. 이미지: image/png|jpeg|webp, 문서: application/pdf|zip|vnd.openxmlformats-...",
            example = "image/png"
    )
    @NotBlank(message = "Content-Type은 필수입니다.")
    @Pattern(
            regexp = "^(image/(png|jpeg|webp)|application/(pdf|zip|vnd\\.openxmlformats-officedocument\\.(wordprocessingml\\.document|spreadsheetml\\.sheet|presentationml\\.presentation))|text/plain)$",
            message = "허용되지 않는 Content-Type입니다."
    )
    private String contentType;

    @Schema(description = "파일 크기 (Byte 단위)", example = "1048576")
    @NotNull(message = "파일 크기는 필수입니다.")
    @Positive(message = "파일 크기는 0보다 커야 합니다.")
    @Max(value = 50 * 1024 * 1024, message = "파일 크기는 최대 50MB까지 허용됩니다.")
    private Long contentLength;

    @Schema(description = "업로드할 카테고리", example = "BLOG")
    @NotNull(message = "업로드 카테고리는 필수입니다.")
    private UploadCategory uploadCategory;

    @Schema(description = "저장 유형. IMAGE → images/, FILE → files/ (기본값: IMAGE)", example = "IMAGE", nullable = true)
    private FileStorageType fileStorageType = FileStorageType.IMAGE;
}
