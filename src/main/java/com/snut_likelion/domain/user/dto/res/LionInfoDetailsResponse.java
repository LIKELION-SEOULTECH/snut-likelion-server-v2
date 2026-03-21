package com.snut_likelion.domain.user.dto.res;

import com.snut_likelion.domain.project.entity.Project;
import com.snut_likelion.domain.user.entity.LionInfo;
import com.snut_likelion.domain.user.entity.Part;
import com.snut_likelion.domain.user.entity.Role;
import com.snut_likelion.global.common.support.RoleConverter;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LionInfoDetailsResponse {

    private int generation;
    private String role;
    private String part;
    private List<ParticipatingProject> projects;

    @Builder
    public LionInfoDetailsResponse(int generation, Role role, Part part, List<Project> projects) {
        this.generation = generation;
        this.role = RoleConverter.convert(role);
        this.part = part.name();
        this.projects = projects.stream().map(ParticipatingProject::from).toList();
    }

    public static LionInfoDetailsResponse of(LionInfo lionInfo, List<Project> projects) {
        return LionInfoDetailsResponse.builder()
                .generation(lionInfo.getGeneration())
                .role(lionInfo.getRole())
                .part(lionInfo.getPart())
                .projects(projects)
                .build();
    }

    @Getter
    @NoArgsConstructor
    public static class ParticipatingProject {
        private Long id;
        private String name;
        private String thumbnailUrl;

        public ParticipatingProject(Long id, String name, String thumbnailUrl) {
            this.id = id;
            this.name = name;
            this.thumbnailUrl = thumbnailUrl;
        }

        public static ParticipatingProject from(Project project) {
            return new ParticipatingProject(
                    project.getId(),
                    project.getName(),
                    project.getThumbnailUrl()
            );
        }
    }
}
