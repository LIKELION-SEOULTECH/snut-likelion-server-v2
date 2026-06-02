package com.snut_likelion.admin.project.dto.res;

import com.snut_likelion.domain.project.entity.ProjectCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "관리자 프로젝트 목록 페이지 응답")
public class ProjectPageResponse {

    @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
    private int page;

    @Schema(description = "페이지 크기", example = "10")
    private int size;

    @Schema(description = "전체 항목 수", example = "42")
    private long totalElements;

    @Schema(description = "전체 페이지 수", example = "5")
    private int totalPages;

    @Schema(description = "프로젝트 목록")
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
    @Schema(description = "관리자 프로젝트 목록 항목")
    public static class ProjectListResponse {

        @Schema(description = "프로젝트 ID", example = "1")
        private Long id;

        @Schema(description = "프로젝트 이름", example = "멋사 웹 서비스")
        private String name;

        @Schema(description = "프로젝트 기수", example = "14")
        private int generation;

        @Schema(description = "프로젝트 카테고리 (한글 설명)", example = "장기 프로젝트")
        private String category;

        @Schema(description = "생성 일시", example = "2026-06-02T16:19:33")
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
