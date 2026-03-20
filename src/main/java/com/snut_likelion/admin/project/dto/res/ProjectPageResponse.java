package com.snut_likelion.admin.project.dto.res;

import com.snut_likelion.domain.project.entity.ProjectCategory;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectPageResponse {

    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private List<ProjectListResponse> content;

    @Builder
    public ProjectPageResponse(int page, int size, long totalElements, int totalPages, List<ProjectListResponse> content) {
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.content = content;
    }

    public static ProjectPageResponse from(Page<ProjectListResponse> page) {
        return ProjectPageResponse.builder()
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .content(page.getContent())
                .build();
    }

    @Getter
    @NoArgsConstructor
    public static class ProjectListResponse {

        private Long id;
        private String name;
        private int generation;
        private String category;
        private LocalDateTime createAt;

        @Builder
        public ProjectListResponse(Long id, String name, int generation, ProjectCategory category, LocalDateTime createAt) {
            this.id = id;
            this.name = name;
            this.generation = generation;
            this.category = category.getDescription();
            this.createAt = createAt;
        }
    }
}
