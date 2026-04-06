package com.snut_likelion.domain.project.service;

import com.snut_likelion.domain.project.dto.res.ProjectDetailResponse;
import com.snut_likelion.domain.project.dto.res.ProjectResponse;
import com.snut_likelion.domain.project.entity.Project;
import com.snut_likelion.domain.project.entity.ProjectCategory;
import com.snut_likelion.domain.project.entity.ProjectParticipation;
import com.snut_likelion.domain.project.exception.ProjectErrorCode;
import com.snut_likelion.domain.file.service.FileUploadService;
import com.snut_likelion.domain.project.infra.ProjectQueryRepository;
import com.snut_likelion.domain.project.infra.ProjectRepository;
import com.snut_likelion.domain.user.entity.LionInfo;
import com.snut_likelion.domain.user.entity.Part;
import com.snut_likelion.domain.user.entity.Role;
import com.snut_likelion.domain.user.entity.User;
import com.snut_likelion.global.error.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectQueryServiceTest {


    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectQueryRepository projectQueryRepository;

    @Mock
    private FileUploadService fileUploadService;

    @InjectMocks
    private ProjectQueryService projectQueryService;

    @Test
    void getAllProjects_returnsMappedResponses() {
        // Given
        Integer generation = 1;
        ProjectCategory category = ProjectCategory.IDEATHON;

        Project p1 = Project.builder()
                .id(100L)
                .name("프로젝트1")
                .generation(generation)
                .category(category)
                .build();
        p1.setImages(List.of("https://cdn.cn/image1.jpg", "https://cdn.cn/image2.jpg"));

        Project p2 = Project.builder()
                .id(200L)
                .name("프로젝트2")
                .generation(generation)
                .category(category)
                .build();
        when(projectQueryRepository.findAllByGenerationAndCategory(generation, category))
                .thenReturn(List.of(p1, p2));

        // When
        List<ProjectResponse> responses = projectQueryService.getAllProjects(generation, category);

        // Then
        assertThat(responses).hasSize(2)
                .extracting(ProjectResponse::getId, ProjectResponse::getName, ProjectResponse::getGeneration, ProjectResponse::getCategory, ProjectResponse::getThumbnailUrl)
                .containsExactly(
                        tuple(100L, "프로젝트1", generation, category.getDescription(), "https://cdn.cn/image1.jpg"),
                        tuple(200L, "프로젝트2", generation, category.getDescription(), null)
                );
    }

    @Test
    void getAllProjects_whenEmpty_returnsEmptyList() {
        // Given
        when(projectQueryRepository.findAllByGenerationAndCategory(anyInt(), any()))
                .thenReturn(List.of());

        // When
        List<ProjectResponse> responses = projectQueryService.getAllProjects(2, ProjectCategory.HACKATHON);

        // Then
        assertThat(responses).isEmpty();
    }

    @Test
    void getProjectById_whenExists_returnsDetail() {
        // Given
        Long id = 1L;
        int generation = 13;
        Project project = Project.builder()
                .id(id)
                .name("제목")
                .intro("소개")
                .description("상세 설명")
                .generation(generation)
                .websiteUrl("https://example.com")
                .category(ProjectCategory.HACKATHON)
                .build();
        project.setImages(List.of("https://cdn.cn/image1.jpg", "https://cdn.cn/image2.jpg"));
        project.setTags(List.of("Java", "Spring"));


        LionInfo li1 = LionInfo.of(generation, Part.BACKEND, Role.ROLE_USER);
        LionInfo li2 = LionInfo.of(generation, Part.FRONTEND, Role.ROLE_USER);
        LionInfo li3 = LionInfo.of(generation, Part.AI, Role.ROLE_USER);

        User u1 = User.builder()
                .id(1L)
                .username("user1")
                .build();
        u1.addLionInfo(li1);
        User u2 = User.builder()
                .id(2L)
                .username("user2")
                .build();
        u2.addLionInfo(li2);
        User u3 = User.builder()
                .id(3L)
                .username("user3")
                .build();
        u3.addLionInfo(li3);

        ProjectParticipation pp1 = new ProjectParticipation(li1, project);
        ProjectParticipation pp2 = new ProjectParticipation(li2, project);
        ProjectParticipation pp3 = new ProjectParticipation(li3, project);

        project.addParticipation(pp1);
        project.addParticipation(pp2);
        project.addParticipation(pp3);

        when(projectRepository.findById(id)).thenReturn(Optional.of(project));

        // When
        ProjectDetailResponse detail = projectQueryService.getProjectById(id);

        // Then
        assertAll(
                () -> assertThat(detail.getId()).isEqualTo(id),
                () -> assertThat(detail.getName()).isEqualTo("제목"),
                () -> assertThat(detail.getIntro()).isEqualTo("소개"),
                () -> assertThat(detail.getDescription()).isEqualTo("상세 설명"),
                () -> assertThat(detail.getGeneration()).isEqualTo(generation),
                () -> assertThat(detail.getWebsiteUrl()).isEqualTo("https://example.com"),
                () -> assertThat(detail.getPlaystoreUrl()).isNull(),
                () -> assertThat(detail.getAppstoreUrl()).isNull(),
                () -> assertThat(detail.getTags()).containsExactly("JAVA", "SPRING"),
                () -> assertThat(detail.getImageUrls()).containsExactly("https://cdn.cn/image1.jpg", "https://cdn.cn/image2.jpg"),
                () -> assertThat(detail.getCategory()).isEqualTo(ProjectCategory.HACKATHON.getDescription())
        );
    }

    @Test
    void getProjectById_whenNotFound_throwsNotFound() {
        // Given
        Long id = 99L;
        when(projectRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> projectQueryService.getProjectById(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(ProjectErrorCode.NOT_FOUND_PROJECT.getMessage());
    }
}