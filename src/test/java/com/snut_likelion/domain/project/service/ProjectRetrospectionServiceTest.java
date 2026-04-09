package com.snut_likelion.domain.project.service;

import com.snut_likelion.domain.project.dto.req.CreateRetrospectionRequest;
import com.snut_likelion.domain.project.dto.res.RetrospectionResponse;
import com.snut_likelion.domain.project.entity.Project;
import com.snut_likelion.domain.project.entity.ProjectCategory;
import com.snut_likelion.domain.project.exception.ProjectErrorCode;
import com.snut_likelion.domain.project.infra.ProjectParticipationRepository;
import com.snut_likelion.domain.project.infra.ProjectRepository;
import com.snut_likelion.domain.project.infra.ProjectRetrospectionRepository;
import com.snut_likelion.domain.user.entity.LionInfo;
import com.snut_likelion.domain.user.entity.Part;
import com.snut_likelion.domain.user.entity.User;
import com.snut_likelion.domain.user.repository.LionInfoRepository;
import com.snut_likelion.domain.user.repository.UserRepository;
import com.snut_likelion.global.error.exception.ExistingResourceException;
import com.snut_likelion.global.error.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectRetrospectionServiceTest {

    @Mock private ProjectRetrospectionRepository projectRetrospectionRepository;
    @Mock private ProjectParticipationRepository projectParticipationRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private LionInfoRepository lionInfoRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private ProjectRetrospectionService projectRetrospectionService;

    // ── create ───────────────────────────────────────────────────────────────

    @Test
    void create_success_createsParticipationAndRetrospection() {
        // Given
        Long projectId = 1L;
        Long userId = 10L;
        int generation = 14;

        Project project = Project.builder()
                .id(projectId)
                .name("LikeLion App")
                .intro("한 줄 소개")
                .description("상세 설명")
                .generation(generation)
                .category(ProjectCategory.HACKATHON)
                .build();
        project.setImages(List.of("images/projects/thumbnail.png"));

        LionInfo lionInfo = mock(LionInfo.class);
        when(lionInfo.getGeneration()).thenReturn(generation);
        when(lionInfo.getPart()).thenReturn(Part.BACKEND);

        User writer = mock(User.class);
        when(writer.getId()).thenReturn(userId);
        when(writer.getUsername()).thenReturn("홍길동");
        when(writer.getLionInfos()).thenReturn(List.of(lionInfo));

        CreateRetrospectionRequest req = new CreateRetrospectionRequest("이번 프로젝트 회고입니다.");

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectRetrospectionRepository.findByWriter_IdAndProject_Id(userId, projectId))
                .thenReturn(Optional.empty());
        when(lionInfoRepository.findByUser_IdAndGeneration(userId, generation))
                .thenReturn(Optional.of(lionInfo));
        when(userRepository.findById(userId)).thenReturn(Optional.of(writer));

        // When
        RetrospectionResponse response = projectRetrospectionService.create(projectId, userId, req);

        // Then
        assertThat(response.getContent()).isEqualTo("이번 프로젝트 회고입니다.");
        assertThat(response.getWriter().getName()).isEqualTo("홍길동");
        assertThat(response.getWriter().getPart()).isEqualTo(Part.BACKEND.name());
        assertThat(project.getRetrospections()).hasSize(1);
        assertThat(project.getParticipations()).hasSize(1);
    }

    @Test
    void create_projectNotFound_throwsNotFoundException() {
        // Given
        Long projectId = 99L;
        Long userId = 10L;

        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        CreateRetrospectionRequest req = new CreateRetrospectionRequest("회고 내용");

        // When / Then
        assertThatThrownBy(() -> projectRetrospectionService.create(projectId, userId, req))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(ProjectErrorCode.NOT_FOUND_PROJECT.getMessage());
    }

    @Test
    void create_retrospectionAlreadyExists_throwsExistingResourceException() {
        // Given
        Long projectId = 1L;
        Long userId = 10L;

        Project project = Project.builder()
                .id(projectId)
                .name("LikeLion App")
                .intro("소개")
                .description("설명")
                .generation(14)
                .category(ProjectCategory.HACKATHON)
                .build();
        project.setImages(List.of("images/projects/thumbnail.png"));

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectRetrospectionRepository.findByWriter_IdAndProject_Id(userId, projectId))
                .thenReturn(Optional.of(mock(com.snut_likelion.domain.project.entity.ProjectRetrospection.class)));

        CreateRetrospectionRequest req = new CreateRetrospectionRequest("회고 내용");

        // When / Then
        assertThatThrownBy(() -> projectRetrospectionService.create(projectId, userId, req))
                .isInstanceOf(ExistingResourceException.class)
                .hasMessage(ProjectErrorCode.RETROSPECTION_ALREADY_EXISTS.getMessage());
    }

    @Test
    void create_lionInfoNotFound_throwsNotFoundException() {
        // Given
        Long projectId = 1L;
        Long userId = 10L;
        int generation = 14;

        Project project = Project.builder()
                .id(projectId)
                .name("LikeLion App")
                .intro("소개")
                .description("설명")
                .generation(generation)
                .category(ProjectCategory.HACKATHON)
                .build();
        project.setImages(List.of("images/projects/thumbnail.png"));

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectRetrospectionRepository.findByWriter_IdAndProject_Id(userId, projectId))
                .thenReturn(Optional.empty());
        when(lionInfoRepository.findByUser_IdAndGeneration(userId, generation))
                .thenReturn(Optional.empty());

        CreateRetrospectionRequest req = new CreateRetrospectionRequest("회고 내용");

        // When / Then
        assertThatThrownBy(() -> projectRetrospectionService.create(projectId, userId, req))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(ProjectErrorCode.NOT_FOUND_LION_INFO.getMessage());
    }
}
