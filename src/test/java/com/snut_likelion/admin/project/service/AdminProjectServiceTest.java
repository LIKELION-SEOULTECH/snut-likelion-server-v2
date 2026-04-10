package com.snut_likelion.admin.project.service;

import com.snut_likelion.admin.project.dto.res.ProjectPageResponse;
import com.snut_likelion.admin.project.infra.AdminProjectQueryRepository;
import com.snut_likelion.domain.project.service.ProjectCommandService;
import com.snut_likelion.domain.project.service.ProjectRetrospectionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminProjectServiceTest {

    @Mock
    AdminProjectQueryRepository queryRepository;

    @Mock
    ProjectCommandService projectCommandService;

    @Mock
    ProjectRetrospectionService projectRetrospectionService;

    @InjectMocks
    AdminProjectService adminProjectService;

    @Test
    void create_delegatesToProjectCommandService() {
        adminProjectService.create(null);
        verify(projectCommandService).createPresigned(null);
    }

    @Test
    void modify_delegatesToProjectCommandService() {
        adminProjectService.modify(1L, null);
        verify(projectCommandService).modifyPresigned(1L, null);
    }

    @Test
    void deleteProjects_callsRemoveForEach() {
        adminProjectService.deleteProjects(List.of(1L, 2L, 3L));
        verify(projectCommandService, times(3)).remove(anyLong());
    }

    @Test
    void removeImage_delegatesToProjectCommandService() {
        adminProjectService.removeImage(1L, "images/projects/uuid.png");
        verify(projectCommandService).removeImageByKey(1L, "images/projects/uuid.png");
    }

    @Test
    void getAllRetrospectionsByProjectId_delegatesToRetrospectionService() {
        adminProjectService.getAllRetrospectionsByProjectId(1L);
        verify(projectRetrospectionService).getAllByProjectId(1L);
    }

    @Test
    void removeRetrospection_delegatesToRetrospectionService() {
        adminProjectService.removeRetrospection(1L, 2L);
        verify(projectRetrospectionService).remove(1L, 2L);
    }

    @Test
    void getProjectList_returnsPagedResponse() {
        Page<ProjectPageResponse.ProjectListResponse> page = new PageImpl<>(List.of(), PageRequest.of(0, 8), 0);
        when(queryRepository.getProjectList(eq(14), eq("app"), any(PageRequest.class))).thenReturn(page);

        ProjectPageResponse response = adminProjectService.getProjectList(14, 0, "app");

        assertThat(response).isNotNull();
        assertThat(response.getTotalElements()).isZero();
    }
}
