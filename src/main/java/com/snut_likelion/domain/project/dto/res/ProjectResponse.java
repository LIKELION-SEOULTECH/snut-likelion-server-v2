package com.snut_likelion.domain.project.dto.res;

import com.snut_likelion.domain.project.entity.Project;
import com.snut_likelion.domain.project.entity.ProjectCategory;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.function.Function;

import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
public class ProjectResponse {

    private Long id;
    private String name;
    private String description;
    private int generation;
    private List<String> tags;
    private List<String> stacks;
    private String category;
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
