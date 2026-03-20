package com.snut_likelion.domain.user.service;

import com.snut_likelion.domain.file.service.FileUploadService;
import com.snut_likelion.domain.project.entity.Project;
import com.snut_likelion.domain.project.entity.ProjectParticipation;
import com.snut_likelion.domain.user.dto.res.*;
import com.snut_likelion.domain.user.entity.LionInfo;
import com.snut_likelion.domain.user.entity.Role;
import com.snut_likelion.domain.user.entity.User;
import com.snut_likelion.domain.user.exception.UserErrorCode;
import com.snut_likelion.domain.user.repository.LionInfoRepository;
import com.snut_likelion.domain.user.repository.UserRepository;
import com.snut_likelion.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberQueryService {

    private final UserRepository userRepository;
    private final LionInfoRepository lionInfoRepository;
    private final FileUploadService fileUploadService;

    @Transactional(readOnly = true)
    public List<MemberResponse> getMembersByQuery(int generation, boolean isManager) {
        // 어차피 데이터가 많지 않아서 굳이 role에 대한 인덱스 추가 조회보다는 전체 조회 후 애플리케이션 단에서 필터링 해도 성능에 큰 영향은 없음
        List<LionInfo> lionInfos = lionInfoRepository.findAllByGeneration(generation);
        return lionInfos.stream()
                .filter(lionInfo -> this.filteringByRole(lionInfo, isManager))
                .map(lionInfo -> {
                    User user = lionInfo.getUser();
                    List<PortfolioLinkDto> portfolioLinks = user.getPortfolioLinks().stream()
                            .map(PortfolioLinkDto::from).toList();
                    String imageUrl = buildProfileImageUrl(user.getProfileImageUrl());
                    return MemberResponse.of(user, lionInfo, portfolioLinks, imageUrl);
                })
                .sorted(Comparator.comparing(r -> !r.getRole().equals("대표")))
                .toList();
    }

    private boolean filteringByRole(LionInfo lionInfo, boolean isManager) {
        Role role = lionInfo.getRole();
        if (isManager) {
            return role.equals(Role.ROLE_MANAGER) || role.equals(Role.ROLE_ADMIN);
        }
        return role.equals(Role.ROLE_USER);
    }

    @Transactional(readOnly = true)
    public MemberDetailResponse getMyDetails(Long memberId) {
        User user = userRepository.findUserDetailsByUserId(memberId)
                .orElseThrow(() -> new NotFoundException(UserErrorCode.NOT_FOUND));
        List<Integer> generations = lionInfoRepository.findGenerationsByUser_Id(memberId);
        String imageUrl = buildProfileImageUrl(user.getProfileImageUrl());
        return MemberDetailResponse.of(user, generations, imageUrl);
    }

    @Transactional(readOnly = true)
    public MemberDetailResponse getMemberDetailsById(Long memberId) {
        List<Integer> generations = lionInfoRepository.findGenerationsByUser_Id(memberId);
        if (generations.isEmpty()) {
            throw new NotFoundException(UserErrorCode.NOT_FOUND_LION_INFO);
        }

        User user = userRepository.findUserDetailsByUserId(memberId)
                .orElseThrow(() -> new NotFoundException(UserErrorCode.NOT_FOUND));
        String imageUrl = buildProfileImageUrl(user.getProfileImageUrl());
        return MemberDetailResponse.of(user, generations, imageUrl);
    }

    @Transactional(readOnly = true)
    public LionInfoDetailsResponse getMemberLionInfoByIdAndGeneration(Long memberId, int generation) {
        LionInfo lionInfo = lionInfoRepository.findByUser_IdAndGeneration(memberId, generation)
                .orElseThrow(() -> new NotFoundException(UserErrorCode.NOT_FOUND_LION_INFO));
        List<Project> projects = lionInfo.getParticipations().stream() // TODO: N+1 문제 해결
                .map(ProjectParticipation::getProject)
                .toList();

        return LionInfoDetailsResponse.of(lionInfo, projects);
    }

    @Transactional(readOnly = true)
    public List<MemberSearchResponse> searchMembers(String keyword) {
        return userRepository.searchUserByKeyword(keyword).stream()
                .map(r -> r.withProfileImageUrl(buildProfileImageUrl(r.getProfileImageUrl())))
                .toList();
    }

    private String buildProfileImageUrl(String storedFileName) {
        if (storedFileName == null || storedFileName.isBlank()) return null;
        return fileUploadService.buildFileUrl(storedFileName);
    }
}
