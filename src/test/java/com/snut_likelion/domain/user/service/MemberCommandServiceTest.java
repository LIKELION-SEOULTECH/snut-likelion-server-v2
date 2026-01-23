package com.snut_likelion.domain.user.service;

import com.snut_likelion.domain.user.dto.request.UpdateProfileRequest;
import com.snut_likelion.domain.user.entity.User;
import com.snut_likelion.domain.user.exception.UserErrorCode;
import com.snut_likelion.domain.user.repository.LionInfoRepository;
import com.snut_likelion.domain.user.repository.PortfolioLinkRepository;
import com.snut_likelion.domain.user.repository.UserRepository;
import com.snut_likelion.global.auth.model.UserInfo;
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
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MemberCommandServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private LionInfoRepository lionInfoRepository;

    @Mock
    private PortfolioLinkRepository portfolioLinkRepository;

    @InjectMocks
    private MemberCommandService memberCommandService;

    @Mock
    private UserInfo loginUser;

    @Test
    void updateProfile_withImageAndLinks_updatesImageAndLinksAndProfile() {
        // Given
        Long memberId = 1L;
        User user = spy(User.builder()
                .id(memberId)
                .profileImageUrl("http://cdn/old.png")
                .build());
        when(userRepository.findWithLionUserById(memberId))
                .thenReturn(Optional.of(user));

        // Prepare request
        String imageUrl = "http://cdn/new.png";
        UpdateProfileRequest.PortfolioLinkDto pl1 = new UpdateProfileRequest.PortfolioLinkDto("GITHUB", "https://github.com/example");
        UpdateProfileRequest.PortfolioLinkDto pl2 = new UpdateProfileRequest.PortfolioLinkDto("NOTION", "https://notion.com/example");

        UpdateProfileRequest req = UpdateProfileRequest.builder()
                .profileImage(imageUrl)
                .intro("새로운 소개")
                .description("새로운 설명")
                .major("컴퓨터공학")
                .saying("명언")
                .stacks(List.of("JAVA", "SPRING"))
                .portfolioLinks(List.of(pl1, pl2))
                .build();

        // When
        memberCommandService.updateProfile(loginUser, memberId, req);

        // Then
        assertAll(
                () -> verify(user).changeProfileImage("http://cdn/new.png"),
                () -> verify(portfolioLinkRepository).saveAll(anyList()),
                () -> verify(user).setPortfolioLinkList(anyList()),
                () -> verify(user).updateProfile("새로운 소개", "새로운 설명", "컴퓨터공학", "명언", List.of("JAVA", "SPRING"))
        );
    }

    @Test
    void updateProfile_withoutImageAndLinks_onlyUpdatesProfile() {
        Long memberId = 1L;
        User user = User.builder().id(memberId).build();
        when(userRepository.findWithLionUserById(memberId))
                .thenReturn(Optional.of(user));

        UpdateProfileRequest req = UpdateProfileRequest.builder()
                .intro("새로운 소개")
                .description("새로운 설명")
                .major("컴퓨터공학")
                .saying("명언")
                .stacks(List.of("JAVA", "SPRING"))
                .build();


        memberCommandService.updateProfile(loginUser, memberId, req);

        assertAll(
                () -> verify(portfolioLinkRepository, never()).saveAll(any()),
                () -> assertThat(user.getIntro()).isEqualTo("새로운 소개"),
                () -> assertThat(user.getDescription()).isEqualTo("새로운 설명"),
                () -> assertThat(user.getMajor()).isEqualTo("컴퓨터공학"),
                () -> assertThat(user.getSaying()).isEqualTo("명언"),
                () -> assertThat(user.getStacks()).isEqualTo("JAVA, SPRING")
        );
    }

    @Test
    void updateProfile_withBlankImage_doesNotUpdateImage() {
        Long memberId = 1L;
        User user = spy(User.builder()
                .id(memberId)
                .profileImageUrl("http://cdn/old.png")
                .build());
        when(userRepository.findWithLionUserById(memberId))
                .thenReturn(Optional.of(user));

        UpdateProfileRequest req = UpdateProfileRequest.builder()
                .profileImage("   ") // blank string
                .intro("새로운 소개")
                .build();

        memberCommandService.updateProfile(loginUser, memberId, req);

        verify(user, never()).changeProfileImage(anyString());
    }

    @Test
    void withdrawMember_shouldDeleteUser() {
        // Given
        Long memberId = 1L;
        User user = spy(User.builder()
                .id(memberId)
                .profileImageUrl("http://cdn/avatar.png")
                .build());

        when(userRepository.findById(memberId))
                .thenReturn(Optional.of(user));

        // when
        memberCommandService.withdrawMember(loginUser, memberId);

        // then
        verify(userRepository).delete(user);
    }

    @Test
    void updateProfile_userNotFound_throwsNotFound() {
        // Given
        Long memberId = 1L;
        when(userRepository.findWithLionUserById(memberId))
                .thenReturn(Optional.empty());
        UpdateProfileRequest req = mock(UpdateProfileRequest.class);

        // When & Then
        assertThatThrownBy(() -> memberCommandService.updateProfile(loginUser, memberId, req))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(UserErrorCode.NOT_FOUND.getMessage());
    }

    @Test
    void withdrawMember_userNotFound_throwsNotFound() {
        // Given
        Long memberId = 1L;
        when(userRepository.findById(memberId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> memberCommandService.withdrawMember(loginUser, memberId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(UserErrorCode.NOT_FOUND.getMessage());
    }
}