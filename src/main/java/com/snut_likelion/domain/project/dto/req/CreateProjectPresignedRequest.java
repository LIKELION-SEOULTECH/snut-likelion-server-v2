package com.snut_likelion.domain.project.dto.req;

import com.snut_likelion.domain.project.entity.ProjectCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
@Schema(description = "프로젝트 생성 요청 (Presigned URL 기반)")
public class CreateProjectPresignedRequest {

    @Schema(description = "프로젝트 이름", example = "멋사 웹 서비스")
    @NotBlank(message = "프로젝트 이름을 입력해주세요.")
    private String name;

    @Schema(description = "프로젝트 한 줄 소개", example = "멋쟁이사자처럼 공식 웹 서비스")
    @NotBlank(message = "프로젝트 한 줄 소개를 입력해주세요.")
    private String intro;

    @Schema(description = "프로젝트 상세 설명", example = "Spring Boot와 React로 구현한 멋사 통합 관리 시스템입니다.")
    @NotBlank(message = "프로젝트 설명을 입력해주세요.")
    private String description;

    @Schema(description = "프로젝트 기수", example = "14")
    @NotNull(message = "프로젝트 기수를 입력해주세요.")
    @Positive(message = "프로젝트 기수는 1 이상이어야 합니다.")
    private Integer generation;

    @Schema(description = "프로젝트 카테고리", example = "LONG_TERM_PROJECT",
            allowableValues = {"IDEATHON", "HACKATHON", "DEMO_DAY", "LONG_TERM_PROJECT"})
    @NotNull(message = "프로젝트 카테고리를 선택해주세요.")
    private ProjectCategory category;

    @Schema(description = "프로젝트 이미지 storedFileName(S3 key) 목록 (최소 1장)",
            example = "[\"project/uuid-image.jpg\"]")
    @NotEmpty(message = "프로젝트 이미지는 최소 1장 이상 필요합니다.")
    private List<@NotBlank(message = "storedFileName은 빈 값일 수 없습니다.") String> imageStoredFileNames;

    @Schema(description = "웹사이트 URL (선택)", example = "https://example.com", nullable = true)
    private String websiteUrl;

    @Schema(description = "Play 스토어 URL (선택)", example = "https://play.google.com/store/apps/details?id=com.example", nullable = true)
    private String playstoreUrl;

    @Schema(description = "App 스토어 URL (선택)", example = "https://apps.apple.com/app/id000000000", nullable = true)
    private String appstoreUrl;

    @Schema(description = "기술 스택 목록 (선택)", example = "[\"REACT\", \"SPRING\"]", nullable = true)
    private List<@NotBlank(message = "stack은 빈 값일 수 없습니다.") String> stacks;
}
