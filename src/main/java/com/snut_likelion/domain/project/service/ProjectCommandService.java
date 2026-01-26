package com.snut_likelion.domain.project.service;

import com.snut_likelion.domain.file.repository.UploadedFileRepository;
import com.snut_likelion.domain.project.dto.request.*;
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
import com.snut_likelion.global.auth.model.SnutLikeLionUser;
import com.snut_likelion.global.error.exception.BadRequestException;
import com.snut_likelion.global.error.exception.NotFoundException;
import com.snut_likelion.global.provider.FileProvider;
import com.snut_likelion.infra.file.FileErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectCommandService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final FileProvider fileProvider;
    private final LionInfoRepository lionInfoRepository;
    private final ProjectRetrospectionRepository projectRetrospectionRepository;
    private final UploadedFileRepository uploadedFileRepository;

    @Transactional
    public void create(CreateProjectRequest req) {
        Project project = req.toEntityWithValue();
        project.setTags(req.getTags());
        this.storeProjectImages(req.getImages(), project);
        this.connectRetrospections(req.getRetrospections(), project);
        projectRepository.save(project);
    }

    private void connectRetrospections(List<RetrospectionDto> retrospections, Project project) {
        if (retrospections == null || retrospections.isEmpty()) {
            throw new BadRequestException(ProjectErrorCode.RETROSPECTION_IS_NOT_PROVIDED);
        }

        retrospections
                .forEach(retrospectionDto -> {
                    Long memberId = retrospectionDto.getMemberId();

                    LionInfo lionInfo = lionInfoRepository.findByUser_IdAndGeneration(memberId, project.getGeneration())
                            .orElseThrow(() -> new NotFoundException(ProjectErrorCode.NOT_FOUND_LION_INFO));

                    ProjectParticipation projectParticipation = new ProjectParticipation(lionInfo, project);
                    project.addParticipation(projectParticipation);

                    ProjectRetrospection retrospection = this.createRetrospection(retrospectionDto);
                    project.addRetrospection(retrospection);
                });
    }


    @Transactional
    public void modify(Long id, UpdateProjectRequest req) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ProjectErrorCode.NOT_FOUND_PROJECT));

        project.update(req.getName(), req.getIntro(), req.getDescription(), req.getGeneration(), req.getCategory());

        project.setTags(req.getTags());

        this.storeProjectImages(req.getNewImages(), project);

        // 이미 존재하는 회고는 업데이트하고, 새로운 회고는 추가
        this.upsertRetrospections(req.getRetrospections(), project);
    }

    private void upsertRetrospections(List<RetrospectionDto> retrospections, Project project) {
        if (retrospections == null || retrospections.isEmpty()) {
            return;
        }

        retrospections.forEach(retrospection -> {
            projectRetrospectionRepository.findByWriter_IdAndProject_Id(
                            retrospection.getMemberId(), project.getId())
                    .ifPresentOrElse(
                            existingRetrospection ->
                                    existingRetrospection.updateContent(retrospection.getContent()),
                            () -> {
                                Long memberId = retrospection.getMemberId();
                                LionInfo lionInfo = lionInfoRepository.findByUser_IdAndGeneration(memberId, project.getGeneration())
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

    private void storeProjectImages(List<MultipartFile> files, Project project) {
        if (files == null || files.isEmpty()) {
            throw new BadRequestException(ProjectErrorCode.PROJECT_IMAGE_NOT_PROVIDED);
        }

        List<String> imageKeys = new ArrayList<>();

        files.forEach(file -> {
            String contentType = file.getContentType();

            if (contentType == null || !contentType.startsWith("image/")) {
                throw new BadRequestException(ProjectErrorCode.INVALID_FILE_FORMAT);
            }

            String storedName = fileProvider.storeFile(file);
            fileProvider.setTransactionSynchronizationForImage(storedName);

            // URL 저장 금지: key만 저장
            imageKeys.add(storedName);
        });

        project.addImage(imageKeys);
    }

    @Transactional
    public void remove(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ProjectErrorCode.NOT_FOUND_PROJECT));

        // 삭제 전에 key 목록 확보
        List<String> imageKeys = project.getImageUrlList();
        projectRepository.delete(project);

        imageKeys.forEach(fileProvider::deleteFile);
    }


    @Transactional
    public void removeImage(Long id, String imageUrl) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ProjectErrorCode.NOT_FOUND_PROJECT));

        List<String> oldList = project.getImageUrlList();  // ** getImageUrlList -> 나중에 바꾸기... **

        if (oldList.contains(imageUrl)) {
            ArrayList<String> newList = new ArrayList<>(oldList);
            newList.remove(imageUrl);
            project.setImages(newList);

            String storedName = fileProvider.extractImageName(imageUrl);
            fileProvider.deleteFile(storedName);
        }
    }

    @Transactional
    public void removeImageByKey(Long projectId, String imageStoredFileName) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException(ProjectErrorCode.NOT_FOUND_PROJECT));

        // key rule 검증 - 이미 validateKeyRule가 있으면 재사용
        validateKeyRule(imageStoredFileName, "projects");

        // DB에는 이제 key가 들어간다고 가정 (images 컬럼이 key 리스트)
        List<String> oldKeys = project.getImageUrlList(); // ** getImageUrlList -> 나중에 바꾸기... **
        if (oldKeys == null || oldKeys.isEmpty()) {
            return; // 삭제할 파일이 없음 = 이미 삭제된 상태로 간주 (멱등)
        }

        if (!oldKeys.contains(imageStoredFileName)) {
            // 프로젝트에 없는 이미지를 지우려는 요청
            throw new BadRequestException(FileErrorCode.BAD_FILE_URL);
        }

        List<String> newKeys = new ArrayList<>(oldKeys);
        newKeys.remove(imageStoredFileName);
        project.setImages(newKeys);

        // deleteFile에는 key만 넘김 (extractImageName 절대 금지)
        fileProvider.deleteFile(imageStoredFileName);
    }



    // =========================
    // Presigned URL
    // =========================
    /**
     * - Presigned 기반 프로젝트 생성
     * - storedFileName(key)만 받아서
     * - UploadedFile 메타 존재 여부 + key rule 검증 후
     * -  DB에는 key만 저장 (URL 저장 금지)
     */
    @Transactional
    public Long createPresigned(SnutLikeLionUser user, CreateProjectPresignedRequest req) {
        Project project = Project.builder()
                .name(req.getName())
                .intro(req.getIntro())
                .description(req.getDescription())
                .category(req.getCategory())
                .generation(req.getGeneration())
                .build();

        // URL 변환하지 말고 key만 저장
        List<String> imageKeys = validateAndReturnKeys(req.getImageStoredFileNames(), "projects");
        project.setImages(imageKeys); // setImages는 List<String> 받음

        projectRepository.save(project);
        return project.getId();
    }

    /**
     * - Presigned 기반 프로젝트 수정
     * - newImageStoredFileNames가 있을 때만 추가
     */
    @Transactional
    public void modifyPresigned(Long projectId, UpdateProjectPresignedRequest req) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException(ProjectErrorCode.NOT_FOUND_PROJECT));

        project.update(req.getName(), req.getIntro(), req.getDescription(), req.getGeneration(), req.getCategory());

        if (req.getNewImageStoredFileNames() != null && !req.getNewImageStoredFileNames().isEmpty()) {
            List<String> newKeys = validateAndReturnKeys(req.getNewImageStoredFileNames(), "projects");
            project.addImage(newKeys); // addImage는 List<String> 받음
        }
    }

    /**
     * - 공통 검증
     * - key rule: images/{folder}/ 로 시작, '..' 금지 등
     * - UploadedFile(메타) 존재 여부 확인(=STEP4 complete 이후만 허용)
     * - URL로 변환하지 않고 key 그대로 반환
     */
    private List<String> validateAndReturnKeys(List<String> storedFileNames, String folder) {
        return storedFileNames.stream()
                .map(key -> {
                    validateKeyRule(key, folder);

                    boolean exists = uploadedFileRepository.existsByStoredFileName(key);
                    if (!exists) {
                        throw new BadRequestException(FileErrorCode.FILE_NOT_FOUND);
                    }

                    return key; // key 반환
                })
                .collect(Collectors.toList());
    }

    private void validateKeyRule(String storedFileName, String categoryFolder) {
        String prefix = "images/" + categoryFolder + "/";

        if (storedFileName == null || storedFileName.isBlank()
                || !storedFileName.startsWith(prefix)
                || storedFileName.contains("..")) {
            throw new BadRequestException(FileErrorCode.INVALID_FILE_KEY);
        }
    }
}
