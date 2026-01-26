package com.snut_likelion.domain.project.dto.response;

import com.snut_likelion.domain.project.entity.Project;
import com.snut_likelion.domain.project.entity.ProjectCategory;
import com.snut_likelion.global.provider.FileProvider;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
public class ProjectResponse {

    private Long id;
    private String name;
    private String description;
    private int generation;
    private List<String> tags;
    private String category;
    private String thumbnailUrl;

    @Builder
    public ProjectResponse(Long id, String name, String description, int generation, List<String> tags, ProjectCategory category, String thumbnailUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.generation = generation;
        this.tags = tags;
        this.category = (category == null ? null : category.getDescription());
        this.thumbnailUrl = thumbnailUrl;
    }

    public static ProjectResponse from(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .generation(project.getGeneration())
                .tags(project.getTagList())
                .category(project.getCategory())
                .thumbnailUrl(project.getThumbnailUrl())
                .build();
    }

    // Presigned 전환 이후 조회 안전 버전
    public static ProjectResponse from(Project project, FileProvider fileProvider) {
        String thumb = project.getThumbnailUrl();

        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .generation(project.getGeneration())
                .tags(project.getTagList())
                .category(project.getCategory())
                .thumbnailUrl(resolveToUrl(thumb, fileProvider))
                .build();
    }

    private static String resolveToUrl(String keyOrUrl, FileProvider fileProvider) {
        if (keyOrUrl == null || keyOrUrl.isBlank()) return null;
        if (keyOrUrl.startsWith("http://") || keyOrUrl.startsWith("https://")) return keyOrUrl; // 과거 데이터 호환
        return fileProvider.buildImageUrl(keyOrUrl); // key -> URL
    }
}
