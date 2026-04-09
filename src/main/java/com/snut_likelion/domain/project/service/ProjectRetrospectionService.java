package com.snut_likelion.domain.project.service;

import com.snut_likelion.domain.project.dto.req.CreateRetrospectionRequest;
import com.snut_likelion.domain.project.dto.res.RetrospectionResponse;
import com.snut_likelion.domain.project.entity.Project;
import com.snut_likelion.domain.project.entity.ProjectParticipation;
import com.snut_likelion.domain.project.entity.ProjectRetrospection;
import com.snut_likelion.domain.project.exception.ProjectErrorCode;
import com.snut_likelion.domain.project.infra.ProjectParticipationRepository;
import com.snut_likelion.domain.project.infra.ProjectRepository;
import com.snut_likelion.domain.project.infra.ProjectRetrospectionRepository;
import com.snut_likelion.domain.user.entity.LionInfo;
import com.snut_likelion.domain.user.entity.User;
import com.snut_likelion.domain.user.repository.LionInfoRepository;
import com.snut_likelion.domain.user.repository.UserRepository;
import com.snut_likelion.global.error.exception.ExistingResourceException;
import com.snut_likelion.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectRetrospectionService {

    private final ProjectRetrospectionRepository projectRetrospectionRepository;
    private final ProjectParticipationRepository projectParticipationRepository;
    private final ProjectRepository projectRepository;
    private final LionInfoRepository lionInfoRepository;
    private final UserRepository userRepository;

    @Transactional
    public RetrospectionResponse create(Long projectId, Long userId, CreateRetrospectionRequest req) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException(ProjectErrorCode.NOT_FOUND_PROJECT));

        if (projectRetrospectionRepository.findByWriter_IdAndProject_Id(userId, projectId).isPresent()) {
            throw new ExistingResourceException(ProjectErrorCode.RETROSPECTION_ALREADY_EXISTS);
        }

        LionInfo lionInfo = lionInfoRepository.findByUser_IdAndGeneration(userId, project.getGeneration())
                .orElseThrow(() -> new NotFoundException(ProjectErrorCode.NOT_FOUND_LION_INFO));

        User writer = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ProjectErrorCode.NOT_FOUND_PARTICIPANT));

        ProjectRetrospection retrospection = new ProjectRetrospection(req.getContent());
        retrospection.setWriter(writer);

        project.addParticipation(new ProjectParticipation(lionInfo, project));
        project.addRetrospection(retrospection);

        return RetrospectionResponse.from(retrospection);
    }

    @Transactional(readOnly = true)
    public List<RetrospectionResponse> getAllByProjectId(Long projectId) {
        return projectRetrospectionRepository.findByProject_Id(projectId).stream()
                .map(RetrospectionResponse::from)
                .toList();
    }

    @Transactional
    public void remove(Long projectId, Long retrospectionId) {
        ProjectRetrospection projectRetrospection = projectRetrospectionRepository.findById(retrospectionId)
                .orElseThrow(() -> new NotFoundException(ProjectErrorCode.NOT_FOUND_RETROSPECTION));

        User writer = projectRetrospection.getWriter();
        Project project = projectRetrospection.getProject();

        LionInfo lionInfo = writer.getLionInfos().stream()
                .filter(li -> li.getGeneration() == project.getGeneration())
                .findFirst()
                .orElseThrow(() -> new NotFoundException(ProjectErrorCode.NOT_FOUND_LION_INFO));

        ProjectParticipation projectParticipation = projectParticipationRepository.findByLionInfo_IdAndProject_Id(lionInfo.getId(), projectId)
                .orElseThrow(() -> new NotFoundException(ProjectErrorCode.NOT_FOUND_PARTICIPANT));

        project.getParticipations().remove(projectParticipation);
        project.getRetrospections().remove(projectRetrospection);
    }
}
