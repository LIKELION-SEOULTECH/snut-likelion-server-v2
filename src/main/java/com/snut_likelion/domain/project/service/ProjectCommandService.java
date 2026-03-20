package com.snut_likelion.domain.project.service;

import com.snut_likelion.domain.file.dto.UploadCategory;
import com.snut_likelion.domain.file.service.FileUploadService;
import com.snut_likelion.domain.project.dto.request.CreateProjectPresignedRequest;
import com.snut_likelion.domain.project.dto.request.RetrospectionDto;
import com.snut_likelion.domain.project.dto.request.UpdateProjectPresignedRequest;
import com.snut_likelion.domain.project.entity.Project;
import com.snut_likelion.domain.project.entity.ProjectParticipation;
import com.snut_likelion.domain.project.entity.ProjectRetrospection;
import com.snut_likelion.domain.project.exception.ProjectErrorCode;
import com.snut_likelion.domain.project.infra.ProjectRepository;
import com.snut_likelion.domain.project.infra.ProjectRetrospectionRepository;
import com.snut_likelion.domain.user.entity.LionInfo;
import com.snut_likelion.domain.user.entity.User;
import com.snut_likelion.domain.user.repository.LionInfoRepository;
import com.snut_likelion.domain.user.repository.UserRepository;
import com.snut_likelion.global.error.exception.BadRequestException;
import com.snut_likelion.global.error.exception.NotFoundException;
import com.snut_likelion.infra.file.FileErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectCommandService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final FileUploadService fileUploadService; // FileProvider + UploadedFileRepository 대체
    private final LionInfoRepository lionInfoRepository;
    private final ProjectRetrospectionRepository projectRetrospectionRepository;

    /**
     * Presigned 기반 프로젝트 생성
     * - imageStoredFileNames 검증 후 DB에 key만 저장 (URL 저장 금지)
     */
    @Transactional
    public Long createPresigned(CreateProjectPresignedRequest req) {
        Project project = Project.builder()
                .name(req.getName())
                .intro(req.getIntro())
                .description(req.getDescription())
                .category(req.getCategory())
                .generation(req.getGeneration())
                .build();

        // FileUploadService가 검증 담당 (key 규칙 + UploadedFile 존재 확인)
        fileUploadService.validateStoredFileNames(req.getImageStoredFileNames(), UploadCategory.PROJECT);
        project.setImages(req.getImageStoredFileNames());

        projectRepository.save(project);
        return project.getId();
    }

    /**
     * Presigned 기반 프로젝트 수정
     * - newImageStoredFileNames가 있을 때만 이미지 추가
     */
    @Transactional
    public void modifyPresigned(Long projectId, UpdateProjectPresignedRequest req) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException(ProjectErrorCode.NOT_FOUND_PROJECT));

        project.update(req.getName(), req.getIntro(), req.getDescription(),
                req.getGeneration(), req.getCategory());

        if (req.getNewImageStoredFileNames() != null && !req.getNewImageStoredFileNames().isEmpty()) {
            fileUploadService.validateStoredFileNames(
                    req.getNewImageStoredFileNames(), UploadCategory.PROJECT
            );
            project.addImage(req.getNewImageStoredFileNames());
        }
    }

    @Transactional
    public void remove(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ProjectErrorCode.NOT_FOUND_PROJECT));

        // 삭제 전 key 목록 확보 후 프로젝트 삭제, 이후 S3 파일 삭제
        List<String> imageKeys = project.getImageUrlList();
        projectRepository.delete(project);
        imageKeys.forEach(fileUploadService::deleteFile);
    }

    /**
     * 프로젝트 이미지 개별 삭제
     * - storedFileName(key) 기반으로 삭제한다.
     */
    @Transactional
    public void removeImageByKey(Long projectId, String imageStoredFileName) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException(ProjectErrorCode.NOT_FOUND_PROJECT));

        // key 규칙 검증은 FileUploadService에 위임
        fileUploadService.validateStoredFileNames(
                List.of(imageStoredFileName), UploadCategory.PROJECT
        );

        List<String> oldKeys = project.getImageUrlList();
        if (oldKeys == null || oldKeys.isEmpty()) {
            return; // 이미 삭제된 상태 — 멱등 처리
        }

        if (!oldKeys.contains(imageStoredFileName)) {
            throw new BadRequestException(FileErrorCode.BAD_FILE_URL);
        }

        List<String> newKeys = new ArrayList<>(oldKeys);
        newKeys.remove(imageStoredFileName);
        project.setImages(newKeys);

        fileUploadService.deleteFile(imageStoredFileName);
    }

    // 회고 관련 (변경 없음)

    private void connectRetrospections(List<RetrospectionDto> retrospections, Project project) {
        if (retrospections == null || retrospections.isEmpty()) {
            throw new BadRequestException(ProjectErrorCode.RETROSPECTION_IS_NOT_PROVIDED);
        }

        retrospections.forEach(retrospectionDto -> {
            Long memberId = retrospectionDto.getMemberId();
            LionInfo lionInfo = lionInfoRepository.findByUser_IdAndGeneration(memberId, project.getGeneration())
                    .orElseThrow(() -> new NotFoundException(ProjectErrorCode.NOT_FOUND_LION_INFO));

            project.addParticipation(new ProjectParticipation(lionInfo, project));
            project.addRetrospection(this.createRetrospection(retrospectionDto));
        });
    }

    private void upsertRetrospections(List<RetrospectionDto> retrospections, Project project) {
        if (retrospections == null || retrospections.isEmpty()) return;

        retrospections.forEach(retrospection -> {
            projectRetrospectionRepository.findByWriter_IdAndProject_Id(
                            retrospection.getMemberId(), project.getId())
                    .ifPresentOrElse(
                            existing -> existing.updateContent(retrospection.getContent()),
                            () -> {
                                LionInfo lionInfo = lionInfoRepository.findByUser_IdAndGeneration(
                                                retrospection.getMemberId(), project.getGeneration())
                                        .orElseThrow(() -> new NotFoundException(ProjectErrorCode.NOT_FOUND_LION_INFO));
                                project.addParticipation(new ProjectParticipation(lionInfo, project));
                                project.addRetrospection(this.createRetrospection(retrospection));
                            }
                    );
        });
    }

    private ProjectRetrospection createRetrospection(RetrospectionDto retrospection) {
        User writer = userRepository.findById(retrospection.getMemberId())
                .orElseThrow(() -> new NotFoundException(ProjectErrorCode.NOT_FOUND_PARTICIPANT));
        ProjectRetrospection projectRetrospection = new ProjectRetrospection(retrospection.getContent());
        projectRetrospection.setWriter(writer);
        return projectRetrospection;
    }
}
