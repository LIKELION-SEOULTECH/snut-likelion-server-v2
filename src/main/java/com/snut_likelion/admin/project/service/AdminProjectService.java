package com.snut_likelion.admin.project.service;

import com.snut_likelion.admin.project.dto.res.ProjectPageResponse;
import com.snut_likelion.admin.project.infra.AdminProjectQueryRepository;
import com.snut_likelion.domain.project.dto.req.CreateProjectPresignedRequest;
import com.snut_likelion.domain.project.dto.req.UpdateProjectPresignedRequest;
import com.snut_likelion.domain.project.dto.res.RetrospectionResponse;
import com.snut_likelion.domain.project.service.ProjectCommandService;
import com.snut_likelion.domain.project.service.ProjectRetrospectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminProjectService {

    private final int PAGE_SIZE = 8;

    private final AdminProjectQueryRepository queryRepository;
    private final ProjectCommandService projectCommandService;
    private final ProjectRetrospectionService projectRetrospectionService;

    @Transactional(readOnly = true)
    public ProjectPageResponse getProjectList(Integer generation, int page, String keyword) {
        PageRequest pageRequest = PageRequest.of(page, PAGE_SIZE);
        Page<ProjectPageResponse.ProjectListResponse> result =
                queryRepository.getProjectList(generation, keyword, pageRequest);
        return ProjectPageResponse.from(result);
    }

    @Transactional
    public void deleteProjects(List<Long> ids) {
        ids.forEach(projectCommandService::remove);
    }

    @Transactional
    public void create(CreateProjectPresignedRequest req) {
        projectCommandService.createPresigned(req);
    }

    @Transactional
    public void modify(Long projectId, UpdateProjectPresignedRequest req) {
        projectCommandService.modifyPresigned(projectId, req);
    }

    @Transactional
    public void removeImage(Long projectId, String imageStoredFileName) {
        projectCommandService.removeImageByKey(projectId, imageStoredFileName);
    }

    @Transactional(readOnly = true)
    public List<RetrospectionResponse> getAllRetrospectionsByProjectId(Long projectId) {
        return projectRetrospectionService.getAllByProjectId(projectId);
    }

    @Transactional
    public void removeRetrospection(Long projectId, Long retrospectionId) {
        projectRetrospectionService.remove(projectId, retrospectionId);
    }
}
