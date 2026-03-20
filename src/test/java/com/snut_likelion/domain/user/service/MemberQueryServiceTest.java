package com.snut_likelion.domain.user.service;

import com.snut_likelion.domain.file.service.FileUploadService;
import com.snut_likelion.domain.project.entity.Project;
import com.snut_likelion.domain.project.entity.ProjectCategory;
import com.snut_likelion.domain.project.entity.ProjectParticipation;
import com.snut_likelion.domain.user.dto.response.LionInfoDetailsResponse;
import com.snut_likelion.domain.user.dto.response.MemberDetailResponse;
import com.snut_likelion.domain.user.dto.response.MemberResponse;
import com.snut_likelion.domain.user.dto.response.MemberSearchResponse;
import com.snut_likelion.domain.user.entity.*;
import com.snut_likelion.domain.user.exception.UserErrorCode;
import com.snut_likelion.domain.user.repository.LionInfoRepository;
import com.snut_likelion.domain.user.repository.UserRepository;
import com.snut_likelion.global.error.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MemberQueryServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private LionInfoRepository lionInfoRepository;

    @Mock
    private FileUploadService fileUploadService;

    @InjectMocks
    private MemberQueryService memberQueryService;

    private static final String ADMIN_KEY = "images/members/admin-uuid.png";
    private static final String ADMIN_URL = "https://cdn.example.com/admin.png";
    private static final String USER_KEY  = "images/members/user-uuid.png";
    private static final String USER_URL  = "https://cdn.example.com/user.png";

    @Test
    void getMembersByQuery_whenManagerTrue_returnsManagersOnly() {
        // Given
        int generation = 13;
        User adminUser = User.builder()
                .username("admin")
                .profileImageUrl(ADMIN_KEY)
                .intro("대표입니다.")
                .build();
        PortfolioLink github = PortfolioLink.builder()
                .name(PortFolioLinkType.GITHUB)
                .url("https://github.com/admin")
                .build();
        adminUser.setPortfolioLinkList(List.of(github));

        LionInfo adminLionInfo = LionInfo.builder()
                .generation(generation).role(Role.ROLE_ADMIN).part(Part.BACKEND).build();
        adminLionInfo.setUser(adminUser);

        User normalUser = User.builder()
                .username("user").profileImageUrl(USER_KEY).intro("아기사자입니다.").build();
        LionInfo userLionInfo = LionInfo.builder()
                .generation(generation).role(Role.ROLE_USER).part(Part.BACKEND).build();
        userLionInfo.setUser(normalUser);

        when(lionInfoRepository.findAllByGeneration(generation))
                .thenReturn(List.of(adminLionInfo, userLionInfo));
        when(fileUploadService.buildFileUrl(ADMIN_KEY)).thenReturn(ADMIN_URL);

        // When
        List<MemberResponse> result = memberQueryService.getMembersByQuery(generation, true);

        // Then
        assertThat(result).hasSize(1);
        MemberResponse response = result.get(0);
        assertAll(
                () -> assertThat(response.getName()).isEqualTo("admin"),
                () -> assertThat(response.getProfileImageUrl()).isEqualTo(ADMIN_URL),
                () -> assertThat(response.getGeneration()).isEqualTo(generation),
                () -> assertThat(response.getRole()).isEqualTo("대표"),
                () -> assertThat(response.getPart()).isEqualTo(Part.BACKEND),
                () -> assertThat(response.getIntro()).isEqualTo("대표입니다."),
                () -> assertThat(response.getPortfolioLinks()).hasSize(1),
                () -> assertThat(response.getPortfolioLinks().get(0).getName()).isEqualTo(PortFolioLinkType.GITHUB)
        );
    }

    @Test
    void getMembersByQuery_whenManagerFalse_returnsBabyOnly() {
        // Given
        int generation = 13;
        User adminUser = User.builder()
                .username("admin").profileImageUrl(ADMIN_KEY).intro("대표입니다.").build();
        LionInfo adminLionInfo = LionInfo.builder()
                .generation(generation).role(Role.ROLE_ADMIN).part(Part.BACKEND).build();
        adminLionInfo.setUser(adminUser);

        User normalUser = User.builder()
                .username("user").profileImageUrl(USER_KEY).intro("아기사자입니다.").build();
        LionInfo userLionInfo = LionInfo.builder()
                .generation(generation).role(Role.ROLE_USER).part(Part.FRONTEND).build();
        userLionInfo.setUser(normalUser);

        when(lionInfoRepository.findAllByGeneration(generation))
                .thenReturn(List.of(adminLionInfo, userLionInfo));
        when(fileUploadService.buildFileUrl(USER_KEY)).thenReturn(USER_URL);

        // When
        List<MemberResponse> result = memberQueryService.getMembersByQuery(generation, false);

        // Then
        assertThat(result).hasSize(1);
        MemberResponse response = result.get(0);
        assertAll(
                () -> assertThat(response.getName()).isEqualTo("user"),
                () -> assertThat(response.getProfileImageUrl()).isEqualTo(USER_URL),
                () -> assertThat(response.getRole()).isEqualTo("아기사자"),
                () -> assertThat(response.getPart()).isEqualTo(Part.FRONTEND)
        );
    }

    @Test
    void getMembersByQuery_withNullProfileImage_returnsNullUrl() {
        // Given
        int generation = 13;
        User user = User.builder()
                .username("user").profileImageUrl(null).intro("소개").build();
        LionInfo lionInfo = LionInfo.builder()
                .generation(generation).role(Role.ROLE_USER).part(Part.BACKEND).build();
        lionInfo.setUser(user);

        when(lionInfoRepository.findAllByGeneration(generation)).thenReturn(List.of(lionInfo));

        // When
        List<MemberResponse> result = memberQueryService.getMembersByQuery(generation, false);

        // Then
        assertThat(result.get(0).getProfileImageUrl()).isNull();
    }

    @Test
    void getMemberDetailsById_whenExists_returnsMemberDetailWithBuiltUrl() {
        // Given
        Long memberId = 1L;
        User user = User.builder()
                .id(memberId)
                .email("test@test.com")
                .username("user")
                .profileImageUrl(USER_KEY)
                .intro("아기사자입니다.")
                .description("설명입니다.")
                .saying("멋진 명언")
                .stacks("JAVA, SPRING")
                .build();
        when(userRepository.findUserDetailsByUserId(memberId)).thenReturn(Optional.of(user));
        when(lionInfoRepository.findGenerationsByUser_Id(memberId)).thenReturn(List.of(11, 12, 13));
        when(fileUploadService.buildFileUrl(USER_KEY)).thenReturn(USER_URL);

        // When
        MemberDetailResponse detail = memberQueryService.getMemberDetailsById(memberId);

        // Then
        assertAll(
                () -> assertThat(detail.getId()).isEqualTo(memberId),
                () -> assertThat(detail.getEmail()).isEqualTo("test@test.com"),
                () -> assertThat(detail.getName()).isEqualTo("user"),
                () -> assertThat(detail.getProfileImageUrl()).isEqualTo(USER_URL),
                () -> assertThat(detail.getIntro()).isEqualTo("아기사자입니다."),
                () -> assertThat(detail.getDescription()).isEqualTo("설명입니다."),
                () -> assertThat(detail.getStacks()).containsExactly("JAVA", "SPRING"),
                () -> assertThat(detail.getGenerations()).containsExactly(11, 12, 13)
        );
    }

    @Test
    void getMemberDetailsById_whenNotFound_throwsException() {
        // Given
        Long memberId = 99L;
        when(lionInfoRepository.findGenerationsByUser_Id(memberId)).thenReturn(List.of(13));
        when(userRepository.findUserDetailsByUserId(memberId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> memberQueryService.getMemberDetailsById(memberId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(UserErrorCode.NOT_FOUND.getMessage());
    }

    @Test
    void getMemberDetailsById_whenNoLionInfo_throwsException() {
        // Given
        Long memberId = 99L;
        when(lionInfoRepository.findGenerationsByUser_Id(memberId)).thenReturn(List.of());

        // When & Then
        assertThatThrownBy(() -> memberQueryService.getMemberDetailsById(memberId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(UserErrorCode.NOT_FOUND_LION_INFO.getMessage());
    }

    @Test
    void getMemberLionInfoByIdAndGeneration_whenExists_returnsDetails() {
        // Given
        Long memberId = 1L;
        int generation = 13;

        LionInfo userLionInfo = LionInfo.builder()
                .generation(generation).role(Role.ROLE_USER).part(Part.BACKEND).build();
        User user = User.builder()
                .id(memberId).username("user").build();
        user.addLionInfo(userLionInfo);

        Project p1 = Project.builder()
                .id(1L).name("Project 1").intro("Intro 1").description("Desc 1")
                .generation(generation).category(ProjectCategory.HACKATHON).build();
        Project p2 = Project.builder()
                .id(2L).name("Project 2").intro("Intro 2").description("Desc 2")
                .generation(generation).category(ProjectCategory.IDEATHON).build();

        userLionInfo.addProjectParticipation(new ProjectParticipation(userLionInfo, p1));
        userLionInfo.addProjectParticipation(new ProjectParticipation(userLionInfo, p2));

        when(lionInfoRepository.findByUser_IdAndGeneration(memberId, generation))
                .thenReturn(Optional.of(userLionInfo));

        // When
        LionInfoDetailsResponse infoDetails = memberQueryService.getMemberLionInfoByIdAndGeneration(memberId, generation);

        // Then
        assertAll(
                () -> assertThat(infoDetails.getGeneration()).isEqualTo(generation),
                () -> assertThat(infoDetails.getRole()).isEqualTo("아기사자"),
                () -> assertThat(infoDetails.getPart()).isEqualTo(Part.BACKEND.name()),
                () -> assertThat(infoDetails.getProjects()).hasSize(2),
                () -> assertThat(infoDetails.getProjects())
                        .extracting("id", "name")
                        .containsExactly(tuple(1L, "Project 1"), tuple(2L, "Project 2"))
        );
    }

    @Test
    void getMemberLionInfoByIdAndGeneration_whenNotFound_throwsException() {
        // Given
        Long memberId = 999L;
        int generation = 999;
        when(lionInfoRepository.findByUser_IdAndGeneration(memberId, generation))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> memberQueryService.getMemberLionInfoByIdAndGeneration(memberId, generation))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(UserErrorCode.NOT_FOUND_LION_INFO.getMessage());
    }

    @Test
    void searchMembers_convertsStoredFileNameToUrl() {
        // Given
        String keyword = "John";
        MemberSearchResponse msr1 = MemberSearchResponse.builder()
                .id(1L).name("John").part(Part.BACKEND).generation(13).profileImageUrl(ADMIN_KEY).build();
        MemberSearchResponse msr2 = MemberSearchResponse.builder()
                .id(2L).name("Johnny").part(Part.FRONTEND).generation(12).profileImageUrl(null).build();

        when(userRepository.searchUserByKeyword(keyword)).thenReturn(List.of(msr1, msr2));
        when(fileUploadService.buildFileUrl(ADMIN_KEY)).thenReturn(ADMIN_URL);

        // When
        List<MemberSearchResponse> result = memberQueryService.searchMembers(keyword);

        // Then
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result.get(0).getProfileImageUrl()).isEqualTo(ADMIN_URL),
                () -> assertThat(result.get(1).getProfileImageUrl()).isNull()
        );
    }

    @Test
    void searchMembers_whenNoMatches_returnsEmptyList() {
        // Given
        when(userRepository.searchUserByKeyword("xyz")).thenReturn(List.of());

        // When
        List<MemberSearchResponse> result = memberQueryService.searchMembers("xyz");

        // Then
        assertThat(result).isEmpty();
    }
}
