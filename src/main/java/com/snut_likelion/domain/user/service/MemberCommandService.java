package com.snut_likelion.domain.user.service;

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

    @Transactional
    @PreAuthorize("@authChecker.isMe(#loginUser, #memberId)")
    public void updateProfile(UserInfo loginUser, Long memberId, UpdateProfileRequest req) {
        User user = userRepository.findWithLionUserById(memberId)
                .orElseThrow(() -> new NotFoundException(UserErrorCode.NOT_FOUND));

        // 프로필 이미지 URL이 있으면 저장 (presigned URL 방식 예정)
        if (req.getProfileImage() != null && !req.getProfileImage().isBlank()) {
            user.changeProfileImage(req.getProfileImage());
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
