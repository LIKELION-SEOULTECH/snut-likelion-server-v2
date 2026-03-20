package com.snut_likelion.domain.user.service;

import com.snut_likelion.domain.file.dto.UploadCategory;
import com.snut_likelion.domain.file.service.FileUploadService;
import com.snut_likelion.domain.user.dto.request.UpdateProfileRequest;
import com.snut_likelion.domain.user.entity.User;
import com.snut_likelion.domain.user.exception.UserErrorCode;
import com.snut_likelion.domain.user.repository.PortfolioLinkRepository;
import com.snut_likelion.domain.user.repository.UserRepository;
import com.snut_likelion.global.auth.model.UserInfo;
import com.snut_likelion.global.error.exception.BadRequestException;
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
    private PortfolioLinkRepository portfolioLinkRepository;

    @Mock
    private FileUploadService fileUploadService;

    @InjectMocks
    private MemberCommandService memberCommandService;

    @Mock
    private UserInfo loginUser;

    private static final String OLD_KEY = "images/members/old-uuid-profile.png";
    private static final String NEW_KEY = "images/members/new-uuid-profile.png";

    @Test
    void updateProfile_withValidStoredFileName_validatesDeletesOldAndSavesNewKey() {
        // Given
        Long memberId = 1L;
        User user = spy(User.builder()
                .id(memberId)
                .profileImageUrl(OLD_KEY)
                .build());
        when(userRepository.findWithLionUserById(memberId)).thenReturn(Optional.of(user));

        UpdateProfileRequest req = UpdateProfileRequest.builder()
                .profileImage(NEW_KEY)
                .intro("새로운 소개")
                .portfolioLinks(List.of())
                .build();

        // When
        memberCommandService.updateProfile(loginUser, memberId, req);

        // Then
        assertAll(
                () -> verify(fileUploadService).validateStoredFileNames(List.of(NEW_KEY), UploadCategory.MEMBER),
                () -> verify(fileUploadService).deleteFile(OLD_KEY),
                () -> verify(user).changeProfileImage(NEW_KEY)
        );
    }

    @Test
    void updateProfile_withNoOldImage_doesNotCallDeleteFile() {
        // Given
        Long memberId = 1L;
        User user = spy(User.builder()
                .id(memberId)
                .profileImageUrl(null) // 기존 이미지 없음
                .build());
        when(userRepository.findWithLionUserById(memberId)).thenReturn(Optional.of(user));

        UpdateProfileRequest req = UpdateProfileRequest.builder()
                .profileImage(NEW_KEY)
                .portfolioLinks(List.of())
                .build();

        // When
        memberCommandService.updateProfile(loginUser, memberId, req);

        // Then
        verify(fileUploadService, never()).deleteFile(any());
        verify(user).changeProfileImage(NEW_KEY);
    }

    @Test
    void updateProfile_withInvalidStoredFileName_throwsBadRequest() {
        // Given
        Long memberId = 1L;
        User user = User.builder().id(memberId).build();
        when(userRepository.findWithLionUserById(memberId)).thenReturn(Optional.of(user));

        String invalidKey = "images/projects/uuid.png"; // MEMBER가 아닌 경로
        doThrow(new BadRequestException(null, "storedFileName 규칙 위반"))
                .when(fileUploadService).validateStoredFileNames(List.of(invalidKey), UploadCategory.MEMBER);

        UpdateProfileRequest req = UpdateProfileRequest.builder()
                .profileImage(invalidKey)
                .portfolioLinks(List.of())
                .build();

        // When & Then
        assertThatThrownBy(() -> memberCommandService.updateProfile(loginUser, memberId, req))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void updateProfile_withImageAndLinks_updatesAllFields() {
        // Given
        Long memberId = 1L;
        User user = spy(User.builder()
                .id(memberId)
                .profileImageUrl(OLD_KEY)
                .build());
        when(userRepository.findWithLionUserById(memberId)).thenReturn(Optional.of(user));

        UpdateProfileRequest.PortfolioLinkDto pl1 = new UpdateProfileRequest.PortfolioLinkDto("GITHUB", "https://github.com/example");
        UpdateProfileRequest req = UpdateProfileRequest.builder()
                .profileImage(NEW_KEY)
                .intro("새로운 소개")
                .description("새로운 설명")
                .major("컴퓨터공학")
                .saying("명언")
                .stacks(List.of("JAVA", "SPRING"))
                .portfolioLinks(List.of(pl1))
                .build();

        // When
        memberCommandService.updateProfile(loginUser, memberId, req);

        // Then
        assertAll(
                () -> verify(fileUploadService).validateStoredFileNames(List.of(NEW_KEY), UploadCategory.MEMBER),
                () -> verify(fileUploadService).deleteFile(OLD_KEY),
                () -> verify(user).changeProfileImage(NEW_KEY),
                () -> verify(portfolioLinkRepository).saveAll(anyList()),
                () -> verify(user).updateProfile("새로운 소개", "새로운 설명", "컴퓨터공학", "명언", List.of("JAVA", "SPRING"))
        );
    }

    @Test
    void updateProfile_withoutImage_onlyUpdatesProfile() {
        // Given
        Long memberId = 1L;
        User user = User.builder().id(memberId).build();
        when(userRepository.findWithLionUserById(memberId)).thenReturn(Optional.of(user));

        UpdateProfileRequest req = UpdateProfileRequest.builder()
                .intro("새로운 소개")
                .description("새로운 설명")
                .major("컴퓨터공학")
                .saying("명언")
                .stacks(List.of("JAVA", "SPRING"))
                .build();

        // When
        memberCommandService.updateProfile(loginUser, memberId, req);

        // Then
        assertAll(
                () -> verifyNoInteractions(fileUploadService),
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
        // Given
        Long memberId = 1L;
        User user = spy(User.builder()
                .id(memberId)
                .profileImageUrl(OLD_KEY)
                .build());
        when(userRepository.findWithLionUserById(memberId)).thenReturn(Optional.of(user));

        UpdateProfileRequest req = UpdateProfileRequest.builder()
                .profileImage("   ") // blank
                .intro("새로운 소개")
                .build();

        // When
        memberCommandService.updateProfile(loginUser, memberId, req);

        // Then
        verifyNoInteractions(fileUploadService);
        verify(user, never()).changeProfileImage(anyString());
    }

    @Test
    void updateProfile_userNotFound_throwsNotFound() {
        // Given
        Long memberId = 1L;
        when(userRepository.findWithLionUserById(memberId)).thenReturn(Optional.empty());
        UpdateProfileRequest req = mock(UpdateProfileRequest.class);

        // When & Then
        assertThatThrownBy(() -> memberCommandService.updateProfile(loginUser, memberId, req))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(UserErrorCode.NOT_FOUND.getMessage());
    }

    @Test
    void withdrawMember_shouldDeleteUser() {
        // Given
        Long memberId = 1L;
        User user = User.builder().id(memberId).build();
        when(userRepository.findById(memberId)).thenReturn(Optional.of(user));

        // When
        memberCommandService.withdrawMember(loginUser, memberId);

        // Then
        verify(userRepository).delete(user);
    }

    @Test
    void withdrawMember_userNotFound_throwsNotFound() {
        // Given
        Long memberId = 1L;
        when(userRepository.findById(memberId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> memberCommandService.withdrawMember(loginUser, memberId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(UserErrorCode.NOT_FOUND.getMessage());
    }
}
