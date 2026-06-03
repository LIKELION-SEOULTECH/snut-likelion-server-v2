package com.snut_likelion.domain.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snut_likelion.domain.blog.repository.BlogPostRepository;
import com.snut_likelion.domain.project.dto.res.RetrospectionResponse;
import com.snut_likelion.domain.project.infra.ProjectRepository;
import com.snut_likelion.domain.project.service.ProjectRetrospectionService;
import com.snut_likelion.domain.recruitment.infra.ApplicationRepository;
import com.snut_likelion.domain.user.repository.LionInfoRepository;
import com.snut_likelion.global.auth.checker.AuthChecker;
import com.snut_likelion.global.auth.jwt.JwtService;
import com.snut_likelion.global.auth.model.SnutLikeLionUser;
import com.snut_likelion.global.auth.model.UserInfo;
import com.snut_likelion.global.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectRetrospectionController.class)
@Import({SecurityConfig.class, AuthChecker.class})
class ProjectRetrospectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProjectRetrospectionService projectRetrospectionService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AuthenticationProvider authenticationProvider;

    // AuthChecker(@Import)는 실제 빈을 사용한다. isMe는 순수 로직이지만
    // 생성자 의존 레포는 컨텍스트 구성을 위해 목으로 채운다.
    @MockBean
    private LionInfoRepository lionInfoRepository;

    @MockBean
    private ApplicationRepository applicationRepository;

    @MockBean
    private BlogPostRepository blogPostRepository;

    @MockBean
    private ProjectRepository projectRepository;

    private static final Long PROJECT_ID = 7L;

    private RequestPostProcessor login(Long memberId, String role) {
        UserInfo userInfo = UserInfo.builder()
                .id(memberId)
                .email("user" + memberId + "@test.com")
                .role(role)
                .build();
        SnutLikeLionUser principal = SnutLikeLionUser.from(userInfo);
        return SecurityMockMvcRequestPostProcessors.authentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    private String body(Long memberId, String content) throws Exception {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("memberId", memberId);
        map.put("content", content);
        return objectMapper.writeValueAsString(map);
    }

    private String bodyContentOnly(String content) throws Exception {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("content", content);
        return objectMapper.writeValueAsString(map);
    }

    // ── 위임(delegation) ────────────────────────────────────────────────

    @Test
    void createRetrospection_delegatesWithBodyMemberId_notLoginUserId() throws Exception {
        // Given — 매니저(id=99)가 다른 멤버(id=42)의 회고를 등록
        when(projectRetrospectionService.create(anyLong(), anyLong(), any()))
                .thenReturn(RetrospectionResponse.builder().id(1L).content("내용").build());

        // When & Then — 서비스에는 로그인 사용자(99)가 아니라 body의 memberId(42)가 위임돼야 한다
        mockMvc.perform(post("/api/v1/projects/{projectId}/retrospections", PROJECT_ID)
                        .with(login(99L, "ROLE_MANAGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(42L, "이번 프로젝트 회고")))
                .andExpect(status().isCreated());

        verify(projectRetrospectionService).create(eq(PROJECT_ID), eq(42L), any());
    }

    // ── 인가(isMe 가드) ──────────────────────────────────────────────────

    @Test
    void createRetrospection_userWithOtherMemberId_forbidden() throws Exception {
        // Given — 일반 사용자(id=5)가 타인(id=42)의 회고를 작성하려 함

        // When & Then — isMe 가드로 차단되어 403, 서비스는 호출되지 않아야 한다
        mockMvc.perform(post("/api/v1/projects/{projectId}/retrospections", PROJECT_ID)
                        .with(login(5L, "ROLE_USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(42L, "타인 명의 회고")))
                .andExpect(status().isForbidden());

        verify(projectRetrospectionService, never()).create(anyLong(), anyLong(), any());
    }

    @Test
    void createRetrospection_userWithOwnMemberId_created() throws Exception {
        // Given — 일반 사용자(id=5)가 본인(id=5)의 회고를 작성
        when(projectRetrospectionService.create(anyLong(), anyLong(), any()))
                .thenReturn(RetrospectionResponse.builder().id(1L).content("내용").build());

        // When & Then — 본인 memberId이면 isMe 통과 → 201
        mockMvc.perform(post("/api/v1/projects/{projectId}/retrospections", PROJECT_ID)
                        .with(login(5L, "ROLE_USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(5L, "본인 회고")))
                .andExpect(status().isCreated());

        verify(projectRetrospectionService).create(eq(PROJECT_ID), eq(5L), any());
    }

    // ── 입력 검증(validation) ─────────────────────────────────────────────

    @Test
    void createRetrospection_missingMemberId_badRequest() throws Exception {
        // Given — memberId 없이 요청 (가드와 분리해 검증만 보기 위해 매니저 주체 사용)

        // When & Then — @NotNull 위반으로 400, 서비스는 호출되지 않아야 한다
        mockMvc.perform(post("/api/v1/projects/{projectId}/retrospections", PROJECT_ID)
                        .with(login(99L, "ROLE_MANAGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyContentOnly("memberId 누락 회고")))
                .andExpect(status().isBadRequest());

        verify(projectRetrospectionService, never()).create(anyLong(), anyLong(), any());
    }

    @Test
    void createRetrospection_blankContent_badRequest() throws Exception {
        // Given — content 공백 (@NotEmpty 위반)

        // When & Then — 400, 서비스 미호출
        mockMvc.perform(post("/api/v1/projects/{projectId}/retrospections", PROJECT_ID)
                        .with(login(5L, "ROLE_USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(5L, "")))
                .andExpect(status().isBadRequest());

        verify(projectRetrospectionService, never()).create(anyLong(), anyLong(), any());
    }
}
