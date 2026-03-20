package com.snut_likelion.domain.blog.service;

import com.snut_likelion.domain.blog.dto.response.BlogDetailResponse;
import com.snut_likelion.domain.blog.dto.response.BlogSummaryPageResponse;
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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlogQueryService {

    private final UserRepository userRepo;
    private final BlogPostRepository postRepo;
    private final FileUploadService fileUploadService; // key → URL 변환에 사용

    // PUBLISHED 목록
    public BlogSummaryPageResponse getPostList(Category category, int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page, size);

        Page<BlogPost> posts = (keyword == null || keyword.isBlank())
                ? postRepo.findByStatusAndCategoryOrderByUpdatedAtDesc(
                        PostStatus.PUBLISHED, category, pageable)
                : postRepo.findByStatusAndCategoryAndTitleContainingIgnoreCaseOrderByUpdatedAtDesc(
                        PostStatus.PUBLISHED, category, keyword, pageable);

        // key → URL 변환 함수를 람다로 전달
        return BlogSummaryPageResponse.from(posts, fileUploadService::buildFileUrl);
    }

    // 단건 조회
    public BlogDetailResponse getPostDetail(Long id) {
        BlogPost p = postRepo.findById(id)
                .orElseThrow(() -> new NotFoundException(BlogErrorCode.POST_NOT_FOUND));

        if (p.getStatus() == PostStatus.DRAFT) {
            throw new NotFoundException(BlogErrorCode.POST_NOT_FOUND);
        }

        return BlogDetailResponse.from(p, fileUploadService::buildFileUrl);
    }

    // 임시저장 불러오기
    public BlogDetailResponse loadDraft(UserInfo author) {
        User user = userRepo.findById(author.getId())
                .orElseThrow(() -> new NotFoundException(UserErrorCode.NOT_FOUND));
        BlogPost draft = postRepo.findByAuthorAndStatus(user, PostStatus.DRAFT)
                .orElseThrow(() -> new NotFoundException(BlogErrorCode.DRAFT_NOT_FOUND));
        return BlogDetailResponse.from(draft, fileUploadService::buildFileUrl);
    }

    // 내가 쓴 글 목록
    public BlogSummaryPageResponse getMyPosts(UserInfo me, int page, int size) {
        User author = userRepo.findById(me.getId())
                .orElseThrow(() -> new NotFoundException(UserErrorCode.NOT_FOUND));

        Pageable pageable = PageRequest.of(page, size);
        Page<BlogPost> posts = postRepo.findByStatusAndAuthorOrderByUpdatedAtDesc(
                PostStatus.PUBLISHED, author, pageable);

        return BlogSummaryPageResponse.from(posts, fileUploadService::buildFileUrl);
    }
}
