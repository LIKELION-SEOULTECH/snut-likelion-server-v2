package com.snut_likelion.domain.blog.service;

import com.snut_likelion.domain.blog.entity.BlogPost;
import com.snut_likelion.domain.blog.entity.Category;
import com.snut_likelion.domain.blog.entity.PostStatus;
import com.snut_likelion.domain.blog.exception.BlogErrorCode;
import com.snut_likelion.domain.blog.repository.BlogPostRepository;
import com.snut_likelion.domain.file.service.FileUploadService;
import com.snut_likelion.domain.user.entity.User;
import com.snut_likelion.domain.user.exception.UserErrorCode;
import com.snut_likelion.domain.user.repository.UserRepository;
import com.snut_likelion.global.auth.model.UserInfo;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlogQueryServiceTest {

    @Mock
    UserRepository userRepo;

    @Mock
    BlogPostRepository postRepo;

    @Mock
    FileUploadService fileUploadService;

    @InjectMocks
    BlogQueryService blogQueryService;

    User author;
    UserInfo authorInfo;

    @BeforeEach
    void setup() {
        author = User.builder().id(1L).username("tester").email("t@t.com").build();
        authorInfo = UserInfo.from(author, 14);
    }

    @Test
    void getPostList_noKeyword_callsFindByStatusAndCategory() {
        Page<BlogPost> page = new PageImpl<>(List.of());
        when(postRepo.findByStatusAndCategoryOrderByUpdatedAtDesc(eq(PostStatus.PUBLISHED), eq(Category.UNOFFICIAL), any(PageRequest.class)))
                .thenReturn(page);

        blogQueryService.getPostList(Category.UNOFFICIAL, 0, 8, null);

        verify(postRepo).findByStatusAndCategoryOrderByUpdatedAtDesc(eq(PostStatus.PUBLISHED), eq(Category.UNOFFICIAL), any());
    }

    @Test
    void getPostList_withKeyword_callsKeywordSearch() {
        Page<BlogPost> page = new PageImpl<>(List.of());
        when(postRepo.findByStatusAndCategoryAndTitleContainingIgnoreCaseOrderByUpdatedAtDesc(
                eq(PostStatus.PUBLISHED), eq(Category.UNOFFICIAL), eq("spring"), any(PageRequest.class)))
                .thenReturn(page);

        blogQueryService.getPostList(Category.UNOFFICIAL, 0, 8, "spring");

        verify(postRepo).findByStatusAndCategoryAndTitleContainingIgnoreCaseOrderByUpdatedAtDesc(
                eq(PostStatus.PUBLISHED), eq(Category.UNOFFICIAL), eq("spring"), any());
    }

    @Test
    void getPostDetail_published_success() {
        BlogPost post = BlogPost.builder().title("test").content("content")
                .category(Category.UNOFFICIAL).build();
        ReflectionTestUtils.setField(post, "author", author);
        when(postRepo.findById(1L)).thenReturn(Optional.of(post));

        blogQueryService.getPostDetail(1L);

        verify(postRepo).findById(1L);
    }

    @Test
    void getPostDetail_draft_throws() {
        BlogPost draftPost = BlogPost.builder().title("draft").content("content")
                .status(PostStatus.DRAFT).category(Category.UNOFFICIAL).build();
        when(postRepo.findById(1L)).thenReturn(Optional.of(draftPost));

        assertThatThrownBy(() -> blogQueryService.getPostDetail(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(BlogErrorCode.POST_NOT_FOUND.getMessage());
    }

    @Test
    void getPostDetail_notFound_throws() {
        when(postRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> blogQueryService.getPostDetail(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(BlogErrorCode.POST_NOT_FOUND.getMessage());
    }

    @Test
    void loadDraft_success() {
        BlogPost draft = BlogPost.builder().title("draft").content("content")
                .status(PostStatus.DRAFT).category(Category.UNOFFICIAL).build();
        ReflectionTestUtils.setField(draft, "author", author);
        when(userRepo.findById(1L)).thenReturn(Optional.of(author));
        when(postRepo.findByAuthorAndStatus(author, PostStatus.DRAFT)).thenReturn(Optional.of(draft));

        blogQueryService.loadDraft(authorInfo);

        verify(postRepo).findByAuthorAndStatus(author, PostStatus.DRAFT);
    }

    @Test
    void loadDraft_userNotFound_throws() {
        when(userRepo.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> blogQueryService.loadDraft(authorInfo))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(UserErrorCode.NOT_FOUND.getMessage());
    }

    @Test
    void loadDraft_draftNotFound_throws() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(author));
        when(postRepo.findByAuthorAndStatus(author, PostStatus.DRAFT)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> blogQueryService.loadDraft(authorInfo))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(BlogErrorCode.DRAFT_NOT_FOUND.getMessage());
    }

    @Test
    void getMyPosts_userNotFound_throws() {
        when(userRepo.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> blogQueryService.getMyPosts(authorInfo, 0, 8))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(UserErrorCode.NOT_FOUND.getMessage());
    }

    @Test
    void getMyPosts_success() {
        Page<BlogPost> page = new PageImpl<>(List.of());
        when(userRepo.findById(1L)).thenReturn(Optional.of(author));
        when(postRepo.findByStatusAndAuthorOrderByUpdatedAtDesc(eq(PostStatus.PUBLISHED), eq(author), any(PageRequest.class)))
                .thenReturn(page);

        blogQueryService.getMyPosts(authorInfo, 0, 8);

        verify(postRepo).findByStatusAndAuthorOrderByUpdatedAtDesc(eq(PostStatus.PUBLISHED), eq(author), any());
    }
}
