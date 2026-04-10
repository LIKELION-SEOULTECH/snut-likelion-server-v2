package com.snut_likelion.admin.member.service;

import com.snut_likelion.admin.member.dto.req.UpdateMemberRequest;
import com.snut_likelion.admin.member.dto.res.MemberPageResponse;
import com.snut_likelion.admin.member.infra.AdminMemberQueryRepository;
import com.snut_likelion.domain.user.entity.LionInfo;
import com.snut_likelion.domain.user.entity.Part;
import com.snut_likelion.domain.user.entity.Role;
import com.snut_likelion.domain.user.entity.User;
import com.snut_likelion.domain.user.exception.UserErrorCode;
import com.snut_likelion.domain.user.repository.UserRepository;
import com.snut_likelion.global.error.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminMemberServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    AdminMemberQueryRepository queryRepository;

    @InjectMocks
    AdminMemberService adminMemberService;

    User user;
    LionInfo lionInfo14;

    @BeforeEach
    void setup() {
        lionInfo14 = LionInfo.of(14, Part.BACKEND, Role.ROLE_USER);
        user = User.builder().id(1L).username("tester").email("t@t.com").build();
        List<LionInfo> lionInfos = new ArrayList<>();
        lionInfos.add(lionInfo14);
        ReflectionTestUtils.invokeMethod(user, "setLionInfos", lionInfos);
    }

    @Test
    void updateMember_success() {
        when(userRepository.findWithLionUserById(1L)).thenReturn(Optional.of(user));

        UpdateMemberRequest req = UpdateMemberRequest.builder()
                .username("newName").part(Part.FRONTEND).role(Role.ROLE_MANAGER).build();

        adminMemberService.updateMember(1L, 14, req);

        assertThat(user.getUsername()).isEqualTo("newName");
        assertThat(lionInfo14.getPart()).isEqualTo(Part.FRONTEND);
    }

    @Test
    void updateMember_userNotFound_throws() {
        when(userRepository.findWithLionUserById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminMemberService.updateMember(99L, 14,
                UpdateMemberRequest.builder().username("x").part(Part.BACKEND).role(Role.ROLE_USER).build()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(UserErrorCode.NOT_FOUND.getMessage());
    }

    @Test
    void updateMember_lionInfoNotFound_throws() {
        when(userRepository.findWithLionUserById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> adminMemberService.updateMember(1L, 99,
                UpdateMemberRequest.builder().username("x").part(Part.BACKEND).role(Role.ROLE_USER).build()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(UserErrorCode.NOT_FOUND_LION_INFO.getMessage());
    }

    @Test
    void deleteMember_success() {
        when(userRepository.findWithLionUserById(1L)).thenReturn(Optional.of(user));

        adminMemberService.deleteMember(1L, 14);

        assertThat(user.getLionInfos()).isEmpty();
    }

    @Test
    void deleteMember_userNotFound_throws() {
        when(userRepository.findWithLionUserById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminMemberService.deleteMember(99L, 14))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(UserErrorCode.NOT_FOUND.getMessage());
    }

    @Test
    void deleteMember_generationNotFound_throws() {
        when(userRepository.findWithLionUserById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> adminMemberService.deleteMember(1L, 99))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(UserErrorCode.NOT_FOUND_LION_INFO.getMessage());
    }

    @Test
    void getMemberList_returnsPagedResponse() {
        Page<MemberPageResponse.MemberListResponse> page = new PageImpl<>(List.of(), PageRequest.of(0, 8), 0);
        when(queryRepository.getMemberList(eq(14), eq(Part.BACKEND), eq(Role.ROLE_USER), eq("tester"), any(PageRequest.class)))
                .thenReturn(page);

        MemberPageResponse response = adminMemberService.getMemberList(14, Part.BACKEND, Role.ROLE_USER, 0, "tester");

        assertThat(response).isNotNull();
        assertThat(response.getTotalElements()).isZero();
    }
}
