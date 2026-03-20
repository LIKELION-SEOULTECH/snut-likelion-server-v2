package com.snut_likelion.domain.project.service;

import com.snut_likelion.domain.file.dto.UploadCategory;
import com.snut_likelion.domain.file.service.FileUploadService;
import com.snut_likelion.domain.project.dto.request.CreateProjectPresignedRequest;
import com.snut_likelion.domain.project.dto.request.UpdateProjectPresignedRequest;
import com.snut_likelion.domain.project.entity.Project;
import com.snut_likelion.domain.project.entity.ProjectCategory;
import com.snut_likelion.domain.project.exception.ProjectErrorCode;
import com.snut_likelion.domain.project.infra.ProjectRepository;
import com.snut_likelion.domain.project.infra.ProjectRetrospectionRepository;
import com.snut_likelion.domain.user.repository.LionInfoRepository;
import com.snut_likelion.domain.user.repository.UserRepository;
import com.snut_likelion.global.error.exception.BadRequestException;
import com.snut_likelion.global.error.exception.NotFoundException;
import com.snut_likelion.infra.file.FileErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectCommandServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private FileUploadService fileUploadService;
    @Mock private LionInfoRepository lionInfoRepository;
    @Mock private ProjectRetrospectionRepository projectRetrospectionRepository;

    @InjectMocks
    private ProjectCommandService projectCommandService;

    // ── createPresigned ──────────────────────────────────────────────────────

    @Test
    void createPresigned_validRequest_savesProjectAndReturnsId() {
        // Given
        CreateProjectPresignedRequest req = mock(CreateProjectPresignedRequest.class);
        when(req.getName()).thenReturn("LikeLion App");
        when(req.getIntro()).thenReturn("한 줄 소개");
        when(req.getDescription()).thenReturn("상세 설명");
        when(req.getCategory()).thenReturn(ProjectCategory.HACKATHON);
        when(req.getGeneration()).thenReturn(13);
        when(req.getImageStoredFileNames()).thenReturn(List.of("images/projects/uuid-img.png"));

        doNothing().when(fileUploadService).validateStoredFileNames(any(), eq(UploadCategory.PROJECT));
        when(projectRepository.save(any())).thenAnswer(inv -> {
            Project p = inv.getArgument(0);
            return p;
        });

        // When
        projectCommandService.createPresigned(req);

        // Then
        verify(fileUploadService).validateStoredFileNames(List.of("images/projects/uuid-img.png"), UploadCategory.PROJECT);
        verify(projectRepository).save(any(Project.class));
    }

    // ── modifyPresigned ──────────────────────────────────────────────────────

    @Test
    void modifyPresigned_updatesProjectFields() {
        // Given
        Long projectId = 1L;
        Project project = Project.builder()
                .id(projectId)
                .name("Old Name")
                .intro("Old Intro")
                .description("Old Desc")
                .generation(12)
                .category(ProjectCategory.HACKATHON)
                .build();
        project.setImages(List.of("images/projects/old.png"));

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        UpdateProjectPresignedRequest req = mock(UpdateProjectPresignedRequest.class);
        when(req.getName()).thenReturn("New Name");
        when(req.getIntro()).thenReturn("New Intro");
        when(req.getDescription()).thenReturn("New Desc");
        when(req.getGeneration()).thenReturn(13);
        when(req.getCategory()).thenReturn(ProjectCategory.IDEATHON);
        when(req.getNewImageStoredFileNames()).thenReturn(null);

        // When
        projectCommandService.modifyPresigned(projectId, req);

        // Then
        assertThat(project.getName()).isEqualTo("New Name");
        assertThat(project.getIntro()).isEqualTo("New Intro");
        assertThat(project.getGeneration()).isEqualTo(13);
        assertThat(project.getCategory()).isEqualTo(ProjectCategory.IDEATHON);
        verify(fileUploadService, never()).validateStoredFileNames(any(), any());
    }

    @Test
    void modifyPresigned_withNewImages_addsImages() {
        // Given
        Long projectId = 1L;
        Project project = Project.builder()
                .id(projectId)
                .name("Name")
                .intro("Intro")
                .description("Desc")
                .generation(12)
                .category(ProjectCategory.HACKATHON)
                .build();
        project.setImages(List.of("images/projects/old.png"));

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        List<String> newKeys = List.of("images/projects/new1.png", "images/projects/new2.png");
        UpdateProjectPresignedRequest req = mock(UpdateProjectPresignedRequest.class);
        when(req.getName()).thenReturn(null);
        when(req.getIntro()).thenReturn(null);
        when(req.getDescription()).thenReturn(null);
        when(req.getGeneration()).thenReturn(null);
        when(req.getCategory()).thenReturn(null);
        when(req.getNewImageStoredFileNames()).thenReturn(newKeys);

        doNothing().when(fileUploadService).validateStoredFileNames(newKeys, UploadCategory.PROJECT);

        // When
        projectCommandService.modifyPresigned(projectId, req);

        // Then
        verify(fileUploadService).validateStoredFileNames(newKeys, UploadCategory.PROJECT);
        assertThat(project.getImageUrlList()).contains("images/projects/new1.png", "images/projects/new2.png");
    }

    @Test
    void modifyPresigned_notFound_throwsNotFoundException() {
        // Given
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());
        UpdateProjectPresignedRequest req = mock(UpdateProjectPresignedRequest.class);

        // When / Then
        assertThatThrownBy(() -> projectCommandService.modifyPresigned(99L, req))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(ProjectErrorCode.NOT_FOUND_PROJECT.getMessage());
    }

    // ── remove ───────────────────────────────────────────────────────────────

    @Test
    void remove_deletesProjectAndAllImages() {
        // Given
        Long projectId = 1L;
        Project project = Project.builder()
                .id(projectId)
                .name("Name").intro("Intro").description("Desc")
                .generation(13).category(ProjectCategory.HACKATHON)
                .build();
        project.setImages(List.of("images/projects/a.png", "images/projects/b.png"));

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        // When
        projectCommandService.remove(projectId);

        // Then
        verify(projectRepository).delete(project);
        verify(fileUploadService).deleteFile("images/projects/a.png");
        verify(fileUploadService).deleteFile("images/projects/b.png");
    }

    @Test
    void remove_notFound_throwsNotFoundException() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectCommandService.remove(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(ProjectErrorCode.NOT_FOUND_PROJECT.getMessage());
    }

    // ── removeImageByKey ─────────────────────────────────────────────────────

    @Test
    void removeImageByKey_removesTargetImageAndDeletesFromS3() {
        // Given
        Long projectId = 1L;
        String targetKey = "images/projects/uuid-target.png";
        String otherKey  = "images/projects/uuid-other.png";

        Project project = Project.builder()
                .id(projectId)
                .name("Name").intro("Intro").description("Desc")
                .generation(13).category(ProjectCategory.HACKATHON)
                .build();
        project.setImages(List.of(targetKey, otherKey));

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        doNothing().when(fileUploadService).validateStoredFileNames(List.of(targetKey), UploadCategory.PROJECT);

        // When
        projectCommandService.removeImageByKey(projectId, targetKey);

        // Then
        assertThat(project.getImageUrlList()).containsExactly(otherKey);
        verify(fileUploadService).deleteFile(targetKey);
    }

    @Test
    void removeImageByKey_imageNotInProject_throwsBadRequest() {
        // Given
        Long projectId = 1L;
        String presentKey = "images/projects/uuid-present.png";
        String absentKey  = "images/projects/uuid-absent.png";

        Project project = Project.builder()
                .id(projectId)
                .name("Name").intro("Intro").description("Desc")
                .generation(13).category(ProjectCategory.HACKATHON)
                .build();
        project.setImages(List.of(presentKey));

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        doNothing().when(fileUploadService).validateStoredFileNames(List.of(absentKey), UploadCategory.PROJECT);

        // When / Then
        assertThatThrownBy(() -> projectCommandService.removeImageByKey(projectId, absentKey))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(FileErrorCode.BAD_FILE_URL.getMessage());
    }

    @Test
    void removeImageByKey_emptyImages_returnsIdempotently() {
        // Given
        Long projectId = 1L;
        String key = "images/projects/uuid-img.png";

        Project project = Project.builder()
                .id(projectId)
                .name("Name").intro("Intro").description("Desc")
                .generation(13).category(ProjectCategory.HACKATHON)
                .build();
        // 이미지 없는 상태

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        doNothing().when(fileUploadService).validateStoredFileNames(List.of(key), UploadCategory.PROJECT);

        // When (예외 없이 종료)
        projectCommandService.removeImageByKey(projectId, key);

        // Then
        verify(fileUploadService, never()).deleteFile(any());
    }
}
