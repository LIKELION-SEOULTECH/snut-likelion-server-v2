package com.snut_likelion.domain.user.service;

import com.snut_likelion.domain.file.dto.UploadCategory;
import com.snut_likelion.domain.file.service.FileUploadService;
import com.snut_likelion.domain.user.dto.request.UpdateProfileRequest;
import com.snut_likelion.domain.user.entity.PortfolioLink;
import com.snut_likelion.domain.user.entity.User;
import com.snut_likelion.domain.user.exception.UserErrorCode;
import com.snut_likelion.domain.user.repository.PortfolioLinkRepository;
import com.snut_likelion.domain.user.repository.UserRepository;
import com.snut_likelion.global.auth.model.UserInfo;
import com.snut_likelion.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberCommandService {

    private final UserRepository userRepository;
    private final PortfolioLinkRepository portfolioLinkRepository;
    private final FileUploadService fileUploadService;

    @Transactional
    @PreAuthorize("@authChecker.isMe(#loginUser, #memberId)")
    public void updateProfile(UserInfo loginUser, Long memberId, UpdateProfileRequest req) {
        User user = userRepository.findWithLionUserById(memberId)
                .orElseThrow(() -> new NotFoundException(UserErrorCode.NOT_FOUND));

        if (req.getProfileImage() != null && !req.getProfileImage().isBlank()) {
            fileUploadService.validateStoredFileNames(
                    List.of(req.getProfileImage()), UploadCategory.MEMBER
            );
            // 기존 프로필 이미지 S3에서 삭제
            if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isBlank()) {
                fileUploadService.deleteFile(user.getProfileImageUrl());
            }
            user.changeProfileImage(req.getProfileImage()); // storedFileName 저장
        }

        if (!req.getPortfolioLinks().isEmpty()) {
            this.connectPortfolioLinks(req.getPortfolioLinks(), user);
        }

        user.updateProfile(req.getIntro(), req.getDescription(), req.getMajor(), req.getSaying(), req.getStacks());
    }

    private void connectPortfolioLinks(List<UpdateProfileRequest.PortfolioLinkDto> portfolioLinkDtos, User user) {
        user.getPortfolioLinks().clear();
        List<PortfolioLink> portfolioLinkList = portfolioLinkDtos.stream()
                .map(UpdateProfileRequest.PortfolioLinkDto::toEntity)
                .toList();
        user.setPortfolioLinkList(portfolioLinkList);
        portfolioLinkRepository.saveAll(portfolioLinkList); // TODO: N+1 문제 해결
    }

    @Transactional
    @PreAuthorize("@authChecker.isMe(#loginUser, #memberId)")
    public void withdrawMember(UserInfo loginUser, Long memberId) {
        User user = userRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(UserErrorCode.NOT_FOUND));
        userRepository.delete(user);
    }
}
