package com.snut_likelion.domain.project.dto.res;

import com.snut_likelion.domain.project.entity.Project;
import com.snut_likelion.domain.project.entity.ProjectCategory;
import com.snut_likelion.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.function.Function;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "프로젝트 상세 응답")
public class ProjectDetailResponse {

    @Schema(description = "프로젝트 ID", example = "1")
    private Long id;

    @Schema(description = "프로젝트 이름", example = "멋사 웹 서비스")
    private String name;

    @Schema(description = "프로젝트 한 줄 소개", example = "멋쟁이사자처럼 공식 웹 서비스")
    private String intro;

    @Schema(description = "프로젝트 상세 설명", example = "Spring Boot와 React로 구현한 멋사 통합 관리 시스템입니다.")
    private String description;

    @Schema(description = "프로젝트 기수", example = "14")
    private int generation;

    @Schema(description = "웹사이트 URL", example = "https://example.com", nullable = true)
    private String websiteUrl;

    @Schema(description = "Play 스토어 URL", example = "https://play.google.com/store/apps/details?id=com.example", nullable = true)
    private String playstoreUrl;

    @Schema(description = "App 스토어 URL", example = "https://apps.apple.com/app/id000000000", nullable = true)
    private String appstoreUrl;

    @Schema(description = "태그 목록", example = "[\"AWARD\"]")
    private List<String> tags;

    @Schema(description = "기술 스택 목록", example = "[\"REACT\", \"SPRING\"]")
    private List<String> stacks;

    @Schema(description = "참여 멤버 목록")
    private List<Participant> members;

    @Schema(description = "프로젝트 카테고리 (한글 설명)", example = "장기 프로젝트")
    private String category;

    @Schema(description = "프로젝트 이미지 URL 목록", example = "[\"https://bucket.s3.ap-northeast-2.amazonaws.com/project/uuid-image.jpg\"]")
    private List<String> imageUrls; // 응답은 항상 URL (key 아님)

    @Builder
    public ProjectDetailResponse(Long id, String name, String intro, String description,
                                 int generation, String websiteUrl, String playstoreUrl,
                                 String appstoreUrl, List<String> tags, List<String> stacks,
                                 List<Participant> members, ProjectCategory category, List<String> imageUrls) {
        this.id = id;
        this.name = name;
        this.intro = intro;
        this.description = description;
        this.generation = generation;
        this.websiteUrl = websiteUrl;
        this.playstoreUrl = playstoreUrl;
        this.appstoreUrl = appstoreUrl;
        this.tags = tags;
        this.stacks = stacks;
        this.members = members;
        this.category = (category == null ? null : category.getDescription());
        this.imageUrls = imageUrls;
    }

    /**
     * @param keyToUrl storedFileName(key) → URL 변환 함수
     *                 ex) fileUploadService::buildFileUrl
     *                 과거 URL 데이터는 그대로 통과 (http로 시작하는 경우)
     */
    public static ProjectDetailResponse from(Project project, Function<String, String> keyToUrl) {
        List<String> imageUrls = project.getImageUrlList().stream()
                .map(v -> resolveToUrl(v, keyToUrl))
                .toList();

        return ProjectDetailResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .intro(project.getIntro())
                .description(project.getDescription())
                .generation(project.getGeneration())
                .websiteUrl(project.getWebsiteUrl())
                .playstoreUrl(project.getPlaystoreUrl())
                .appstoreUrl(project.getAppstoreUrl())
                .category(project.getCategory())
                .tags(project.getTagList())
                .stacks(project.getStackList())
                .imageUrls(imageUrls)
                .members(project.getParticipations().stream()
                        .map(p -> p.getLionInfo().getUser())
                        .map(Participant::from)
                        .toList())
                .build();
    }

    // key 또는 과거 URL을 URL로 변환
    private static String resolveToUrl(String keyOrUrl, Function<String, String> keyToUrl) {
        if (keyOrUrl == null || keyOrUrl.isBlank()) return null;
        if (keyOrUrl.startsWith("http://") || keyOrUrl.startsWith("https://")) return keyOrUrl;
        return keyToUrl.apply(keyOrUrl);
    }

    @Getter
    @Schema(description = "프로젝트 참여 멤버")
    public static class Participant {

        @Schema(description = "회원 ID", example = "42")
        private Long id;

        @Schema(description = "회원 이름", example = "홍길동")
        private String username;

        @Builder
        public Participant(Long id, String username) {
            this.id = id;
            this.username = username;
        }

        public static Participant from(User user) {
            return Participant.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .build();
        }
    }
}
