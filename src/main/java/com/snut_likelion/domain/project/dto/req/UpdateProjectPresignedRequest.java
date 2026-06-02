package com.snut_likelion.domain.project.dto.req;

import com.snut_likelion.domain.project.entity.ProjectCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
@Schema(description = "프로젝트 수정 요청 (Presigned URL 기반, 모든 필드 선택 / 최소 1개 필수)")
public class UpdateProjectPresignedRequest {

    @Schema(description = "프로젝트 이름", example = "멋사 웹 서비스", nullable = true)
    private String name;

    @Schema(description = "프로젝트 한 줄 소개", example = "멋쟁이사자처럼 공식 웹 서비스", nullable = true)
    private String intro;

    @Schema(description = "프로젝트 상세 설명", example = "Spring Boot와 React로 구현한 멋사 통합 관리 시스템입니다.", nullable = true)
    private String description;

    @Schema(description = "프로젝트 기수", example = "14", nullable = true)
    private Integer generation;

    @Schema(description = "태그 목록", example = "[\"AWARD\"]", nullable = true)
    private List<@NotBlank(message = "tag는 빈 값일 수 없습니다.") String> tags;

    @Schema(description = "프로젝트 카테고리", example = "LONG_TERM_PROJECT",
            allowableValues = {"IDEATHON", "HACKATHON", "DEMO_DAY", "LONG_TERM_PROJECT"}, nullable = true)
    private ProjectCategory category;

    @Schema(description = "웹사이트 URL", example = "https://example.com", nullable = true)
    private String websiteUrl;

    @Schema(description = "Play 스토어 URL", example = "https://play.google.com/store/apps/details?id=com.example", nullable = true)
    private String playstoreUrl;

    @Schema(description = "App 스토어 URL", example = "https://apps.apple.com/app/id000000000", nullable = true)
    private String appstoreUrl;

    @Schema(description = "추가할 이미지 storedFileName(S3 key) 목록", example = "[\"project/uuid-image.jpg\"]", nullable = true)
    private List<@NotBlank(message = "storedFileName은 빈 값일 수 없습니다.") String> newImageStoredFileNames;

    @Schema(description = "기술 스택 목록", example = "[\"REACT\", \"SPRING\"]", nullable = true)
    private List<@NotBlank(message = "stack은 빈 값일 수 없습니다.") String> stacks;

    @AssertTrue(message = "수정할 값이 없습니다.")
    private boolean isAnyFieldProvided() {
        return (name != null && !name.isBlank())
                || (intro != null && !intro.isBlank())
                || (description != null && !description.isBlank())
                || generation != null
                || (tags != null && !tags.isEmpty())
                || category != null
                || (websiteUrl != null && !websiteUrl.isBlank())
                || (playstoreUrl != null && !playstoreUrl.isBlank())
                || (appstoreUrl != null && !appstoreUrl.isBlank())
                || (newImageStoredFileNames != null && !newImageStoredFileNames.isEmpty())
                || (stacks != null && !stacks.isEmpty());
    }
}
