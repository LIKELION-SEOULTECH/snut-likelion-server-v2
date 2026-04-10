package com.snut_likelion.admin.blog.service;

import com.snut_likelion.admin.blog.dto.res.BlogPageResponse;
import com.snut_likelion.admin.blog.infra.AdminBlogQueryRepository;
import com.snut_likelion.domain.blog.service.BlogCommandService;
import com.snut_likelion.domain.user.entity.User;
import com.snut_likelion.global.auth.model.UserInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminBlogServiceTest {

    @Mock
    AdminBlogQueryRepository queryRepository;

    @Mock
    BlogCommandService blogCommandService;

    @InjectMocks
    AdminBlogService adminBlogService;

    UserInfo userInfo;

    @BeforeEach
    void setup() {
        User user = User.builder().id(1L).username("admin").email("a@a.com").build();
        userInfo = UserInfo.from(user, 14);
    }

    @Test
    void create_delegatesToBlogCommandService() {
        adminBlogService.create(null, userInfo);
        verify(blogCommandService).createPost(null, userInfo, true);
    }

    @Test
    void modify_delegatesToBlogCommandService() {
        adminBlogService.modify(1L, null, userInfo);
        verify(blogCommandService).updatePost(1L, null, userInfo, true);
    }

    @Test
    void delete_delegatesToBlogCommandService() {
        adminBlogService.delete(1L, userInfo);
        verify(blogCommandService).deletePost(1L, userInfo);
    }

    @Test
    void deleteBlogs_callsDeleteForEach() {
        adminBlogService.deleteBlogs(List.of(1L, 2L, 3L), userInfo);
        verify(blogCommandService, times(3)).deletePost(org.mockito.ArgumentMatchers.anyLong(), eq(userInfo));
    }

    @Test
    void getBlogList_returnsPagedResponse() {
        Page<BlogPageResponse.BlogListResponse> page = new PageImpl<>(List.of(), PageRequest.of(0, 8), 0);
        when(queryRepository.getBlogList(eq("UNOFFICIAL"), eq("spring"), any(PageRequest.class))).thenReturn(page);

        BlogPageResponse response = adminBlogService.getBlogList("UNOFFICIAL", 0, "spring");

        assertThat(response).isNotNull();
        assertThat(response.getTotalElements()).isZero();
    }

    private static <T> T eq(T value) {
        return org.mockito.ArgumentMatchers.eq(value);
    }
}
