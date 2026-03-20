package com.snut_likelion.domain.notice.service;


import com.snut_likelion.domain.file.dto.FileStorageType;
import com.snut_likelion.domain.file.dto.UploadCategory;
import com.snut_likelion.domain.file.entity.UploadedFile;
import com.snut_likelion.domain.file.service.FileUploadService;
import com.snut_likelion.domain.notice.dto.event.NoticeCreatedEvent;
import com.snut_likelion.domain.notice.dto.request.CreateNoticeRequest;
import com.snut_likelion.domain.notice.dto.request.UpdateNoticeRequest;
import com.snut_likelion.domain.notice.dto.response.NoticeDetailResponse;
import com.snut_likelion.domain.notice.dto.response.NoticeListResponse;
import com.snut_likelion.domain.notice.dto.response.NoticePageResponse;
import com.snut_likelion.domain.notice.entity.AttachmentType;
import com.snut_likelion.domain.notice.entity.Notice;
import com.snut_likelion.domain.notice.entity.NoticeAttachment;
import com.snut_likelion.domain.notice.exception.NoticeErrorCode;
import com.snut_likelion.domain.notice.repository.NoticeAttachmentRepository;
import com.snut_likelion.domain.notice.repository.NoticeRepository;
import com.snut_likelion.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final NoticeAttachmentRepository attachmentRepository;
    private final FileUploadService fileUploadService;
    private final ApplicationEventPublisher publisher;

    @Transactional
    public Long createNotice(CreateNoticeRequest request) {
        // 첨부파일 유효성 검증
        fileUploadService.validateStoredFileNames(
                request.getImageStoredFileNames(), UploadCategory.NOTICE, FileStorageType.IMAGE);
        fileUploadService.validateStoredFileNames(
                request.getFileStoredFileNames(), UploadCategory.NOTICE, FileStorageType.FILE);

        Notice notice = noticeRepository.save(request.toEntity());

        // 이미지 첨부파일 저장
        addAttachments(notice, request.getImageStoredFileNames(), AttachmentType.IMAGE);

        // 일반 파일 첨부파일 저장
        addAttachments(notice, request.getFileStoredFileNames(), AttachmentType.FILE);

        publisher.publishEvent(new NoticeCreatedEvent(notice.getId(), notice.getContent()));
        return notice.getId();
    }

    @Transactional(readOnly = true)
    public NoticePageResponse getNoticePage(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Notice> noticePage = (keyword == null || keyword.isBlank())
                ? noticeRepository.findAllByOrderByPinnedDescUpdatedAtDesc(pageable)
                : noticeRepository.findByTitleContainingIgnoreCaseOrderByPinnedDescUpdatedAtDesc(keyword, pageable);

        List<NoticeListResponse> content = noticePage.getContent()
                .stream()
                .map(NoticeListResponse::from)
                .toList();

        return NoticePageResponse.of(content, noticePage);
    }

    @Transactional(readOnly = true)
    public NoticeDetailResponse getNoticeDetail(Long id) {
        Notice notice = noticeRepository.findByIdWithAttachments(id)
                .orElseThrow(() -> new NotFoundException(NoticeErrorCode.NOT_FOUND));

        Map<String, String> urlMap = buildUrlMap(notice.getAttachments());
        return NoticeDetailResponse.of(notice, urlMap, Map.of());
    }

    @Transactional
    public void updateNotice(Long id, UpdateNoticeRequest request) {
        Notice notice = noticeRepository.findByIdWithAttachments(id)
                .orElseThrow(() -> new NotFoundException(NoticeErrorCode.NOT_FOUND));

        // 새 첨부파일 유효성 검증
        fileUploadService.validateStoredFileNames(
                request.getImageStoredFileNames(), UploadCategory.NOTICE, FileStorageType.IMAGE);
        fileUploadService.validateStoredFileNames(
                request.getFileStoredFileNames(), UploadCategory.NOTICE, FileStorageType.FILE);

        // 삭제 요청된 첨부파일 처리
        if (!request.getAttachmentIdsToDelete().isEmpty()) {
            List<NoticeAttachment> toDelete = notice.getAttachments().stream()
                    .filter(a -> request.getAttachmentIdsToDelete().contains(a.getId()))
                    .toList();
            toDelete.forEach(a -> {
                fileUploadService.deleteFile(a.getStoredFileName());
                notice.getAttachments().remove(a);
            });
        }

        // 새 첨부파일 추가
        addAttachments(notice, request.getImageStoredFileNames(), AttachmentType.IMAGE);
        addAttachments(notice, request.getFileStoredFileNames(), AttachmentType.FILE);

        if (request.getTitle() != null || request.getContent() != null || request.getPinned() != null) {
            notice.update(
                    request.getTitle() != null ? request.getTitle() : notice.getTitle(),
                    request.getContent() != null ? request.getContent() : notice.getContent(),
                    request.getPinned() != null ? request.getPinned() : notice.getPinned()
            );
        }
    }

    @Transactional
    public void deleteNotice(Long id) {
        Notice notice = noticeRepository.findByIdWithAttachments(id)
                .orElseThrow(() -> new NotFoundException(NoticeErrorCode.NOT_FOUND));

        // S3 파일 삭제
        notice.getAttachments().forEach(a -> fileUploadService.deleteFile(a.getStoredFileName()));

        noticeRepository.delete(notice);
    }

    /**
     * 첨부파일 단건 삭제 (관리자 전용)
     */
    @Transactional
    public void removeAttachment(Long noticeId, Long attachmentId) {
        NoticeAttachment attachment = attachmentRepository.findById(attachmentId)
                .filter(a -> a.getNotice().getId().equals(noticeId))
                .orElseThrow(() -> new NotFoundException(NoticeErrorCode.ATTACHMENT_NOT_FOUND));

        fileUploadService.deleteFile(attachment.getStoredFileName());
        attachmentRepository.delete(attachment);
    }

    // 내부 유틸

    private void addAttachments(Notice notice, List<String> storedFileNames, AttachmentType type) {
        if (storedFileNames == null || storedFileNames.isEmpty()) return;

        // UploadedFile에서 originalFileName 조회
        Map<String, String> originalNameMap = fileUploadService.findUploadedFiles(storedFileNames)
                .stream()
                .collect(Collectors.toMap(UploadedFile::getStoredFileName, UploadedFile::getOriginalFileName));

        storedFileNames.forEach(key -> {
            NoticeAttachment attachment = NoticeAttachment.builder()
                    .notice(notice)
                    .attachmentType(type)
                    .storedFileName(key)
                    .originalFileName(originalNameMap.getOrDefault(key, key))
                    .build();
            notice.addAttachment(attachment);
        });
    }

    private Map<String, String> buildUrlMap(List<NoticeAttachment> attachments) {
        return attachments.stream()
                .collect(Collectors.toMap(
                        NoticeAttachment::getStoredFileName,
                        a -> fileUploadService.buildFileUrl(a.getStoredFileName())
                ));
    }
}
