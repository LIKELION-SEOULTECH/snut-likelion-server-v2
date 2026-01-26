package com.snut_likelion.domain.project.dto.response;

import com.snut_likelion.domain.project.entity.Project;
import com.snut_likelion.domain.project.entity.ProjectCategory;
import com.snut_likelion.domain.user.entity.User;
import com.snut_likelion.global.provider.FileProvider;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

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
    private List<Participant> members;
    private String category;
    private List<String> imageUrls;

    @Builder
    public ProjectDetailResponse(Long id, String name, String intro, String description, int generation, String websiteUrl, String playstoreUrl, String appstoreUrl, List<String> tags, List<Participant> members, ProjectCategory category, List<String> imageUrls) {
        this.id = id;
        this.name = name;
        this.intro = intro;
        this.description = description;
        this.generation = generation;
        this.websiteUrl = websiteUrl;
        this.playstoreUrl = playstoreUrl;
        this.appstoreUrl = appstoreUrl;
        this.tags = tags;
        this.members = members;
        this.category = (category == null ? null : category.getDescription());
        this.imageUrls = imageUrls;
    }

    public static ProjectDetailResponse from(Project project) {
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
                .imageUrls(project.getImageUrlList())
                .members(project.getParticipations().stream()
                        .map(p -> p.getLionInfo().getUser())
                        .map(Participant::from)
                        .toList()
                )
                .build();
    }
    // 새 버전: key -> URL 변환해서 내려줌 (Presigned 전환 대응)
    public static ProjectDetailResponse from(Project project, FileProvider fileProvider) {
        List<String> raw = project.getImageUrlList();
        List<String> imageUrls = (raw == null ? List.<String>of() : raw).stream()
                .map(v -> resolveToUrl(v, fileProvider))
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
                .imageUrls(imageUrls) // ✅ 항상 URL
                .members(project.getParticipations().stream()
                        .map(p -> p.getLionInfo().getUser())
                        .map(Participant::from)
                        .toList()
                )
                .build();
    }

    private static String resolveToUrl(String keyOrUrl, FileProvider fileProvider) {
        if (keyOrUrl == null || keyOrUrl.isBlank()) return null;
        if (keyOrUrl.startsWith("http://") || keyOrUrl.startsWith("https://")) return keyOrUrl; // 과거 데이터 호환
        return fileProvider.buildImageUrl(keyOrUrl); // ✅ key -> URL
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
