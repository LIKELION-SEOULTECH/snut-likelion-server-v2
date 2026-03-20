package com.snut_likelion.domain.blog.service;

import com.snut_likelion.domain.blog.dto.request.CreateBlogRequest;
import com.snut_likelion.domain.blog.dto.request.UpdateBlogRequest;
import com.snut_likelion.domain.blog.entity.BlogImage;
import com.snut_likelion.domain.blog.entity.BlogPost;
import com.snut_likelion.domain.blog.entity.PostStatus;
import com.snut_likelion.domain.blog.exception.BlogErrorCode;
import com.snut_likelion.domain.blog.repository.BlogPostRepository;
import com.snut_likelion.domain.file.dto.UploadCategory;
import com.snut_likelion.domain.file.service.FileUploadService;
import com.snut_likelion.domain.user.entity.User;
import com.snut_likelion.domain.user.exception.UserErrorCode;
import com.snut_likelion.domain.user.repository.UserRepository;
import com.snut_likelion.global.auth.model.UserInfo;
import com.snut_likelion.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlogCommandService {

    private final BlogPostRepository postRepo;
    private final UserRepository userRepo;
    private final FileUploadService fileUploadService; // FileProvider 대신 FileUploadService 사용

    // save 이므로 void형태로 반환
    @Transactional
    @PreAuthorize("@authChecker.checkIsOfficialAndManager(#req.category, #author)")
    public Long createPost(CreateBlogRequest req, UserInfo author, boolean submit) {
        BlogPost post = req.toEntity();

        User user = userRepo.findById(author.getId())
                .orElseThrow(() -> new NotFoundException(UserErrorCode.NOT_FOUND));
        post.setAuthor(user);

        // storedFileNames 검증 후 BlogImage 엔티티로 변환하여 설정
        List<String> keys = req.getImageStoredFileNames();
        fileUploadService.validateStoredFileNames(keys, UploadCategory.BLOG);
        post.setImages(BlogImage.listOf(keys));

        post.setTaggedMembers(fetchUsers(req.getTaggedMemberIds()));
        post.setStatus(submit ? PostStatus.PUBLISHED : PostStatus.DRAFT);

        return postRepo.save(post).getId();
    }

    @Transactional
    @PreAuthorize("@authChecker.checkCanModify(#postId, #editor)")
    public void updatePost(Long postId, UpdateBlogRequest req, UserInfo editor, boolean submit) {
        BlogPost post = postRepo.findById(postId)
                .orElseThrow(() -> new NotFoundException(BlogErrorCode.POST_NOT_FOUND));

        post.updatePost(req.getTitle(), req.getContentHtml(), req.getCategory());

        if (req.getTaggedMemberIds() != null) {
            post.setTaggedMembers(fetchUsers(req.getTaggedMemberIds()));
        }

        if (req.getNewImageStoredFileNames() != null) {
            List<String> keys = req.getNewImageStoredFileNames();
            fileUploadService.validateStoredFileNames(keys, UploadCategory.BLOG);
            post.setImages(BlogImage.listOf(keys));
        }

        post.setStatus(submit ? PostStatus.PUBLISHED : PostStatus.DRAFT);
    }

    @Transactional
    @PreAuthorize("@authChecker.checkCanModify(#postId, #editor)")
    public void deletePost(Long postId, UserInfo editor) {
        BlogPost post = postRepo.findById(postId)
                .orElseThrow(() -> new NotFoundException(BlogErrorCode.POST_NOT_FOUND));

        // storedFileName(S3 key)으로 직접 삭제 — extractImageName 사용 금지
        post.getImages()
                .forEach(img -> fileUploadService.deleteFile(img.getStoredFileName()));

        postRepo.delete(post);
    }

    @Transactional
    public void discardDraft(UserInfo author) {
        User user = userRepo.findById(author.getId())
                .orElseThrow(() -> new NotFoundException(UserErrorCode.NOT_FOUND));
        postRepo.deleteByAuthorAndStatus(user, PostStatus.DRAFT);
    }

    private Set<User> fetchUsers(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Set.of();
        return userRepo.findAllById(ids).stream().collect(Collectors.toSet());
    }
}
