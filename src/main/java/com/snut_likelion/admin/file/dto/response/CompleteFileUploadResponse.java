package com.snut_likelion.admin.file.dto.response;

import com.snut_likelion.domain.file.entity.UploadedFile;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
@Schema(description = "업로드 완료 메타 저장 응답")
public class CompleteFileUploadResponse {

    @Schema(description = "등록된 파일의 고유 ID")
    private Long fileId;

    @Schema(description = "S3 key (도메인 API 호출 시 이 값을 사용)", example = "images/notices/uuid-banner.png")
    private String storedFileName;

    @Schema(description = "파일 접근 URL (조회용)", example = "https://bucket.s3.amazonaws.com/images/notices/uuid-banner.png")
    private String fileUrl;

    @Builder
    private CompleteFileUploadResponse(Long fileId, String storedFileName, String fileUrl) {
        this.fileId = fileId;
        this.storedFileName = storedFileName;
        this.fileUrl = fileUrl;
    }

    public static CompleteFileUploadResponse from(UploadedFile saved, String fileUrl) {
        return CompleteFileUploadResponse.builder()
                .fileId(saved.getId())
                .storedFileName(saved.getStoredFileName())
                .fileUrl(fileUrl)
                .build();
    }
}
