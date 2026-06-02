package com.snut_likelion.domain.project.dto.res;

import com.snut_likelion.domain.project.entity.Project;
import com.snut_likelion.domain.project.entity.ProjectCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.function.Function;

import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
@Schema(description = "프로젝트 목록 응답")
public class ProjectResponse {

    @Schema(description = "프로젝트 ID", example = "1")
    private Long id;

    @Schema(description = "프로젝트 이름", example = "멋사 웹 서비스")
    private String name;

    @Schema(description = "프로젝트 상세 설명", example = "Spring Boot와 React로 구현한 멋사 통합 관리 시스템입니다.")
    private String description;

    @Schema(description = "프로젝트 기수", example = "14")
    private int generation;

    @Schema(description = "태그 목록", example = "[\"AWARD\"]")
    private List<String> tags;

    @Schema(description = "기술 스택 목록", example = "[\"REACT\", \"SPRING\"]")
    private List<String> stacks;

    @Schema(description = "프로젝트 카테고리 (한글 설명)", example = "장기 프로젝트")
    private String category;

    @Schema(description = "대표 이미지 URL", example = "https://bucket.s3.ap-northeast-2.amazonaws.com/project/uuid-image.jpg")
    private String thumbnailUrl; // 응답은 항상 URL (key 아님)

    @Builder
    public ProjectResponse(Long id, String name, String description, int generation,
                           List<String> tags, List<String> stacks, ProjectCategory category, String thumbnailUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.generation = generation;
        this.tags = tags;
        this.stacks = stacks;
        this.category = (category == null ? null : category.getDescription());
        this.thumbnailUrl = thumbnailUrl;
    }

    /**
     * @param keyToUrl storedFileName(key) → URL 변환 함수
     *                 ex) fileUploadService::buildFileUrl
     *                 과거 URL 데이터는 그대로 통과 (http로 시작하는 경우)
     */
    public static ProjectResponse from(Project project, Function<String, String> keyToUrl) {
        String thumb = project.getThumbnailUrl();

        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .generation(project.getGeneration())
                .tags(project.getTagList())
                .stacks(project.getStackList())
                .category(project.getCategory())
                .thumbnailUrl(resolveToUrl(thumb, keyToUrl))
                .build();
    }

    // key 또는 과거 URL을 URL로 변환
    private static String resolveToUrl(String keyOrUrl, Function<String, String> keyToUrl) {
        if (keyOrUrl == null || keyOrUrl.isBlank()) return null;
        if (keyOrUrl.startsWith("http://") || keyOrUrl.startsWith("https://")) return keyOrUrl;
        return keyToUrl.apply(keyOrUrl);
    }
}
