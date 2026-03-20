package com.snut_likelion.admin.file.dto.request;

import com.snut_likelion.domain.file.dto.UploadCategory;
import com.snut_likelion.domain.file.entity.UploadedFile;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
@Schema(description = "업로드 완료 후 파일 메타데이터 저장 요청")
public class CompleteFileUploadRequest {

    @Schema(description = "S3에 저장된 실제 키(경로 포함)", example = "images/blogs/uuid-xxx.png")
    @NotBlank(message = "storedFileName은 필수입니다.")
    private String storedFileName;

    @Schema(description = "원본 파일명", example = "my-image.png")
    @NotBlank(message = "originalFileName은 필수입니다.")
    private String originalFileName;

    @Schema(description = "파일 Content-Type (STEP 1 발급 요청과 동일한 값)", example = "image/png")
    @NotBlank(message = "Content-Type은 필수입니다.")
    @Pattern(
            regexp = "^(image/(png|jpeg|webp)|application/(pdf|zip|vnd\\.openxmlformats-officedocument\\.(wordprocessingml\\.document|spreadsheetml\\.sheet|presentationml\\.presentation))|text/plain)$",
            message = "허용되지 않는 Content-Type입니다."
    )
    private String contentType;

    @Schema(description = "파일 크기 (Byte 단위)", example = "1048576")
    @NotNull(message = "contentLength는 필수입니다.")
    @Positive(message = "contentLength는 0보다 커야 합니다.")
    @Max(value = 50 * 1024 * 1024, message = "파일 크기는 최대 50MB까지 허용됩니다.")
    private Long contentLength;

    @Schema(description = "업로드 카테고리", example = "BLOG")
    @NotNull(message = "uploadCategory는 필수입니다.")
    private UploadCategory uploadCategory;

    public UploadedFile toEntity() {
        return UploadedFile.builder()
                .uploadCategory(uploadCategory)
                .storedFileName(storedFileName)
                .originalFileName(originalFileName)
                .contentType(contentType)
                .contentLength(contentLength)
                .build();
    }
}
