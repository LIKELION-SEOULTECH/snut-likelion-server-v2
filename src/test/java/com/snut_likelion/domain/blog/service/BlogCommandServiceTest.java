package com.snut_likelion.domain.blog.service;

import com.snut_likelion.domain.blog.dto.req.CreateBlogRequest;
import com.snut_likelion.domain.blog.dto.req.UpdateBlogRequest;
import com.snut_likelion.domain.blog.entity.BlogImage;
import com.snut_likelion.domain.blog.entity.BlogPost;
import com.snut_likelion.domain.blog.entity.Category;
import com.snut_likelion.domain.blog.entity.PostStatus;
import com.snut_likelion.domain.blog.exception.BlogErrorCode;
import com.snut_likelion.domain.blog.repository.BlogPostRepository;
import com.snut_likelion.domain.file.dto.UploadCategory;
import com.snut_likelion.domain.file.service.FileUploadService;
import com.snut_likelion.domain.user.entity.User;
import com.snut_likelion.domain.user.repository.UserRepository;
import com.snut_likelion.global.auth.model.UserInfo;
import com.snut_likelion.global.error.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlogCommandServiceTest {

    @Mock private BlogPostRepository postRepo;
    @Mock private UserRepository userRepo;
    @Mock private FileUploadService fileUploadService;

    @InjectMocks
    private BlogCommandService blogCommandService;

    private User author;
    private UserInfo authorInfo;

    @BeforeEach
    void setUp() {
        author = User.builder().id(1L).username("tester").email("test@example.com").build();
        authorInfo = mock(UserInfo.class);
    }

    // ── createPost ───────────────────────────────────────────────────────────

    @Test
    void createPost_validImages_savesPostWithImages() {
        // Given
        List<String> keys = List.of("images/blogs/uuid-img1.png", "images/blogs/uuid-img2.png");
        CreateBlogRequest req = CreateBlogRequest.builder()
                .title("제목")
                .contentHtml("<p>내용</p>")
                .category(Category.UNOFFICIAL)
                .imageStoredFileNames(keys)
                .taggedMemberIds(List.of())
                .build();

        when(authorInfo.getId()).thenReturn(1L);
        when(userRepo.findById(1L)).thenReturn(Optional.of(author));
        doNothing().when(fileUploadService).validateStoredFileNames(keys, UploadCategory.BLOG);
        when(postRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // When
        blogCommandService.createPost(req, authorInfo, true);

        // Then
        ArgumentCaptor<BlogPost> captor = ArgumentCaptor.forClass(BlogPost.class);
        verify(postRepo).save(captor.capture());
        BlogPost saved = captor.getValue();

        assertThat(saved.getImages()).hasSize(2)
                .extracting(BlogImage::getStoredFileName)
                .containsExactlyInAnyOrder("images/blogs/uuid-img1.png", "images/blogs/uuid-img2.png");
        assertThat(saved.getStatus()).isEqualTo(PostStatus.PUBLISHED);
        verify(fileUploadService).validateStoredFileNames(keys, UploadCategory.BLOG);
    }

    @Test
    void createPost_asDraft_savesWithDraftStatus() {
        // Given
        CreateBlogRequest req = CreateBlogRequest.builder()
                .title("임시저장 제목")
                .contentHtml("<p>내용</p>")
                .category(Category.UNOFFICIAL)
                .imageStoredFileNames(List.of())
                .taggedMemberIds(List.of())
                .build();

        when(authorInfo.getId()).thenReturn(1L);
        when(userRepo.findById(1L)).thenReturn(Optional.of(author));
        when(postRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // When
        blogCommandService.createPost(req, authorInfo, false);

        // Then
        ArgumentCaptor<BlogPost> captor = ArgumentCaptor.forClass(BlogPost.class);
        verify(postRepo).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(PostStatus.DRAFT);
    }

    // ── updatePost ───────────────────────────────────────────────────────────

    @Test
    void updatePost_withNewImages_replacesImages() {
        // Given
        Long postId = 1L;
        BlogPost post = BlogPost.builder().title("old").content("<p>old</p>").category(Category.UNOFFICIAL).build();
        post.setImages(BlogImage.listOf(List.of("images/blogs/uuid-old.png")));

        when(postRepo.findById(postId)).thenReturn(Optional.of(post));

        List<String> newKeys = List.of("images/blogs/uuid-new.png");
        UpdateBlogRequest req = mock(UpdateBlogRequest.class);
        when(req.getTitle()).thenReturn("새 제목");
        when(req.getContentHtml()).thenReturn("<p>새 내용</p>");
        when(req.getCategory()).thenReturn(Category.OFFICIAL);
        when(req.getTaggedMemberIds()).thenReturn(null);
        when(req.getNewImageStoredFileNames()).thenReturn(newKeys);

        doNothing().when(fileUploadService).validateStoredFileNames(newKeys, UploadCategory.BLOG);

        // When
        blogCommandService.updatePost(postId, req, authorInfo, true);

        // Then
        verify(fileUploadService).validateStoredFileNames(newKeys, UploadCategory.BLOG);
        assertThat(post.getImages())
                .extracting(BlogImage::getStoredFileName)
                .containsExactly("images/blogs/uuid-new.png");
        assertThat(post.getStatus()).isEqualTo(PostStatus.PUBLISHED);
    }

    @Test
    void updatePost_noNewImages_doesNotTouchImages() {
        // Given
        Long postId = 1L;
        BlogPost post = BlogPost.builder().title("old").content("<p>old</p>").category(Category.UNOFFICIAL).build();
        post.setImages(BlogImage.listOf(List.of("images/blogs/uuid-existing.png")));

        when(postRepo.findById(postId)).thenReturn(Optional.of(post));

        UpdateBlogRequest req = mock(UpdateBlogRequest.class);
        when(req.getTitle()).thenReturn("새 제목");
        when(req.getContentHtml()).thenReturn("<p>내용</p>");
        when(req.getCategory()).thenReturn(Category.UNOFFICIAL);
        when(req.getTaggedMemberIds()).thenReturn(null);
        when(req.getNewImageStoredFileNames()).thenReturn(null);

        // When
        blogCommandService.updatePost(postId, req, authorInfo, false);

        // Then
        verify(fileUploadService, never()).validateStoredFileNames(any(), any());
        assertThat(post.getImages()).hasSize(1);
    }

    @Test
    void updatePost_notFound_throwsNotFoundException() {
        when(postRepo.findById(99L)).thenReturn(Optional.empty());
        UpdateBlogRequest req = mock(UpdateBlogRequest.class);

        assertThatThrownBy(() -> blogCommandService.updatePost(99L, req, authorInfo, false))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(BlogErrorCode.POST_NOT_FOUND.getMessage());
    }

    // ── deletePost ───────────────────────────────────────────────────────────

    @Test
    void deletePost_deletesAllImagesFromS3ThenPost() {
        // Given
        Long postId = 1L;
        BlogPost post = BlogPost.builder().title("title").content("<p>content</p>").category(Category.UNOFFICIAL).build();
        post.setImages(BlogImage.listOf(List.of(
                "images/blogs/uuid-a.png",
                "images/blogs/uuid-b.png"
        )));

        when(postRepo.findById(postId)).thenReturn(Optional.of(post));

        // When
        blogCommandService.deletePost(postId, authorInfo);

        // Then
        verify(fileUploadService).deleteFile("images/blogs/uuid-a.png");
        verify(fileUploadService).deleteFile("images/blogs/uuid-b.png");
        verify(postRepo).delete(post);
    }

    @Test
    void deletePost_noImages_deletesPostWithoutS3Call() {
        // Given
        Long postId = 1L;
        BlogPost post = BlogPost.builder().title("title").content("<p>content</p>").category(Category.UNOFFICIAL).build();
        post.setImages(List.of());

        when(postRepo.findById(postId)).thenReturn(Optional.of(post));

        // When
        blogCommandService.deletePost(postId, authorInfo);

        // Then
        verify(fileUploadService, never()).deleteFile(any());
        verify(postRepo).delete(post);
    }

    @Test
    void deletePost_notFound_throwsNotFoundException() {
        when(postRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> blogCommandService.deletePost(99L, authorInfo))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(BlogErrorCode.POST_NOT_FOUND.getMessage());
    }

    // ── discardDraft ─────────────────────────────────────────────────────────

    @Test
    void discardDraft_success_deletesUserDraft() {
        when(authorInfo.getId()).thenReturn(1L);
        when(userRepo.findById(1L)).thenReturn(Optional.of(author));

        blogCommandService.discardDraft(authorInfo);

        verify(postRepo).deleteByAuthorAndStatus(author, PostStatus.DRAFT);
    }

    @Test
    void discardDraft_userNotFound_throwsNotFoundException() {
        when(authorInfo.getId()).thenReturn(99L);
        when(userRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> blogCommandService.discardDraft(authorInfo))
                .isInstanceOf(NotFoundException.class);

        verify(postRepo, never()).deleteByAuthorAndStatus(any(), any());
    }

    // ── createPost user not found ────────────────────────────────────────────

    @Test
    void createPost_userNotFound_throwsNotFoundException() {
        CreateBlogRequest req = CreateBlogRequest.builder()
                .title("제목")
                .contentHtml("<p>내용</p>")
                .category(Category.UNOFFICIAL)
                .imageStoredFileNames(List.of())
                .taggedMemberIds(List.of())
                .build();

        when(authorInfo.getId()).thenReturn(99L);
        when(userRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> blogCommandService.createPost(req, authorInfo, true))
                .isInstanceOf(NotFoundException.class);

        verify(postRepo, never()).save(any());
    }

    // ── updatePost with taggedMembers ────────────────────────────────────────

    @Test
    void updatePost_withTaggedMembers_setsTaggedMembers() {
        Long postId = 1L;
        BlogPost post = BlogPost.builder().title("old").content("<p>old</p>").category(Category.UNOFFICIAL).build();
        post.setImages(List.of());

        when(postRepo.findById(postId)).thenReturn(Optional.of(post));

        User member1 = User.builder().id(10L).username("member1").build();
        User member2 = User.builder().id(11L).username("member2").build();

        UpdateBlogRequest req = mock(UpdateBlogRequest.class);
        when(req.getTitle()).thenReturn("제목");
        when(req.getContentHtml()).thenReturn("<p>내용</p>");
        when(req.getCategory()).thenReturn(Category.UNOFFICIAL);
        when(req.getTaggedMemberIds()).thenReturn(List.of(10L, 11L));
        when(req.getNewImageStoredFileNames()).thenReturn(null);
        when(userRepo.findAllById(List.of(10L, 11L))).thenReturn(List.of(member1, member2));

        blogCommandService.updatePost(postId, req, authorInfo, true);

        assertThat(post.getTaggedMembers()).containsExactlyInAnyOrder(member1, member2);
    }
}
