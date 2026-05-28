package com.snut_likelion.domain.project.dto.res;

import com.snut_likelion.domain.project.entity.Project;
import com.snut_likelion.domain.project.entity.ProjectCategory;
import com.snut_likelion.domain.user.entity.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.function.Function;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectDetailResponse {

    private Long id;
    private String name;
    private String intro;
    private String description;
    private int generation;
    private String websiteUrl;
    private String playstoreUrl;
    private String appstoreUrl;
    private List<String> tags;
    private List<String> stacks;
    private List<Participant> members;
    private String category;
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
    public static class Participant {
        private Long id;
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
