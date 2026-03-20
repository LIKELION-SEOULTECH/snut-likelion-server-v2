package com.snut_likelion.domain.notice.service;

import com.snut_likelion.domain.file.dto.FileStorageType;
import com.snut_likelion.domain.file.dto.UploadCategory;
import com.snut_likelion.domain.file.entity.UploadedFile;
import com.snut_likelion.domain.file.service.FileUploadService;
import com.snut_likelion.domain.notice.dto.request.CreateNoticeRequest;
import com.snut_likelion.domain.notice.dto.request.UpdateNoticeRequest;
import com.snut_likelion.domain.notice.dto.response.NoticeDetailResponse;
import com.snut_likelion.domain.notice.entity.AttachmentType;
import com.snut_likelion.domain.notice.entity.Notice;
import com.snut_likelion.domain.notice.entity.NoticeAttachment;
import com.snut_likelion.domain.notice.exception.NoticeErrorCode;
import com.snut_likelion.domain.notice.repository.NoticeAttachmentRepository;
import com.snut_likelion.domain.notice.repository.NoticeRepository;
import com.snut_likelion.global.error.exception.BadRequestException;
import com.snut_likelion.global.error.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoticeServiceTest {

    @Mock private NoticeRepository noticeRepository;
    @Mock private NoticeAttachmentRepository attachmentRepository;
    @Mock private FileUploadService fileUploadService;
    @Mock private ApplicationEventPublisher publisher;

    @InjectMocks
    private NoticeService noticeService;

    // ── 상수 ──────────────────────────────────────────────────────────────────

    private static final String IMG_KEY_1 = "images/notices/uuid1-banner.png";
    private static final String IMG_KEY_2 = "images/notices/uuid2-thumb.png";
    private static final String FILE_KEY_1 = "files/notices/uuid3-report.pdf";
    private static final String IMG_URL_1  = "https://cdn.example.com/notices/banner.png";
    private static final String IMG_URL_2  = "https://cdn.example.com/notices/thumb.png";
    private static final String FILE_URL_1 = "https://cdn.example.com/notices/report.pdf";

    // ── createNotice ──────────────────────────────────────────────────────────

    @Test
    void createNotice_withNoAttachments_savesNoticeAndPublishesEvent() {
        // Given
        CreateNoticeRequest req = mock(CreateNoticeRequest.class);
        when(req.getImageStoredFileNames()).thenReturn(List.of());
        when(req.getFileStoredFileNames()).thenReturn(List.of());
        when(req.toEntity()).thenReturn(Notice.builder().title("공지").content("내용").pinned(false).build());

        Notice savedNotice = Notice.builder().title("공지").content("내용").build();
        when(noticeRepository.save(any())).thenReturn(savedNotice);

        // When
        noticeService.createNotice(req);

        // Then
        verify(noticeRepository).save(any(Notice.class));
        // NoticeCreatedEvent는 ApplicationEvent가 아니므로 Object 오버로드가 호출됨
        verify(publisher).publishEvent((Object) any());
        // 빈 리스트여도 validate는 호출되지만, deleteFile/findUploadedFiles는 호출되지 않아야 함
        verify(fileUploadService, never()).deleteFile(any());
        verify(fileUploadService, never()).findUploadedFiles(any());
    }

    @Test
    void createNotice_withImageAttachments_validatesAndAddsImages() {
        // Given
        List<String> imageKeys = List.of(IMG_KEY_1, IMG_KEY_2);

        CreateNoticeRequest req = mock(CreateNoticeRequest.class);
        when(req.getImageStoredFileNames()).thenReturn(imageKeys);
        when(req.getFileStoredFileNames()).thenReturn(List.of());
        when(req.toEntity()).thenReturn(Notice.builder().title("공지").content("내용").build());

        Notice savedNotice = Notice.builder().title("공지").content("내용").build();
        when(noticeRepository.save(any())).thenReturn(savedNotice);

        UploadedFile uf1 = UploadedFile.builder()
                .uploadCategory(UploadCategory.NOTICE).storedFileName(IMG_KEY_1)
                .originalFileName("banner.png").contentType("image/png").contentLength(1024L).build();
        UploadedFile uf2 = UploadedFile.builder()
                .uploadCategory(UploadCategory.NOTICE).storedFileName(IMG_KEY_2)
                .originalFileName("thumb.png").contentType("image/png").contentLength(512L).build();
        when(fileUploadService.findUploadedFiles(imageKeys)).thenReturn(List.of(uf1, uf2));

        // When
        noticeService.createNotice(req);

        // Then
        verify(fileUploadService).validateStoredFileNames(imageKeys, UploadCategory.NOTICE, FileStorageType.IMAGE);
        verify(fileUploadService).findUploadedFiles(imageKeys);

        // 이미지 2개가 notice에 추가되었는지 확인
        assertThat(savedNotice.getAttachments()).hasSize(2);
        assertThat(savedNotice.getAttachments())
                .extracting(NoticeAttachment::getAttachmentType)
                .containsOnly(AttachmentType.IMAGE);
        assertThat(savedNotice.getAttachments())
                .extracting(NoticeAttachment::getStoredFileName)
                .containsExactlyInAnyOrder(IMG_KEY_1, IMG_KEY_2);
        assertThat(savedNotice.getAttachments())
                .extracting(NoticeAttachment::getOriginalFileName)
                .containsExactlyInAnyOrder("banner.png", "thumb.png");
    }

    @Test
    void createNotice_withFileAttachments_validatesAndAddsFiles() {
        // Given
        List<String> fileKeys = List.of(FILE_KEY_1);

        CreateNoticeRequest req = mock(CreateNoticeRequest.class);
        when(req.getImageStoredFileNames()).thenReturn(List.of());
        when(req.getFileStoredFileNames()).thenReturn(fileKeys);
        when(req.toEntity()).thenReturn(Notice.builder().title("공지").content("내용").build());

        Notice savedNotice = Notice.builder().title("공지").content("내용").build();
        when(noticeRepository.save(any())).thenReturn(savedNotice);

        UploadedFile uf = UploadedFile.builder()
                .uploadCategory(UploadCategory.NOTICE).storedFileName(FILE_KEY_1)
                .originalFileName("report.pdf").contentType("application/pdf").contentLength(204800L).build();
        when(fileUploadService.findUploadedFiles(fileKeys)).thenReturn(List.of(uf));

        // When
        noticeService.createNotice(req);

        // Then
        verify(fileUploadService).validateStoredFileNames(fileKeys, UploadCategory.NOTICE, FileStorageType.FILE);
        assertThat(savedNotice.getAttachments()).hasSize(1);
        assertThat(savedNotice.getAttachments().get(0).getAttachmentType()).isEqualTo(AttachmentType.FILE);
        assertThat(savedNotice.getAttachments().get(0).getStoredFileName()).isEqualTo(FILE_KEY_1);
        assertThat(savedNotice.getAttachments().get(0).getOriginalFileName()).isEqualTo("report.pdf");
    }

    @Test
    void createNotice_withBothImageAndFile_addsAll() {
        // Given
        List<String> imageKeys = List.of(IMG_KEY_1);
        List<String> fileKeys  = List.of(FILE_KEY_1);

        CreateNoticeRequest req = mock(CreateNoticeRequest.class);
        when(req.getImageStoredFileNames()).thenReturn(imageKeys);
        when(req.getFileStoredFileNames()).thenReturn(fileKeys);
        when(req.toEntity()).thenReturn(Notice.builder().title("공지").content("내용").build());

        Notice savedNotice = Notice.builder().title("공지").content("내용").build();
        when(noticeRepository.save(any())).thenReturn(savedNotice);

        when(fileUploadService.findUploadedFiles(imageKeys)).thenReturn(List.of(
                UploadedFile.builder().uploadCategory(UploadCategory.NOTICE)
                        .storedFileName(IMG_KEY_1).originalFileName("banner.png")
                        .contentType("image/png").contentLength(1024L).build()
        ));
        when(fileUploadService.findUploadedFiles(fileKeys)).thenReturn(List.of(
                UploadedFile.builder().uploadCategory(UploadCategory.NOTICE)
                        .storedFileName(FILE_KEY_1).originalFileName("report.pdf")
                        .contentType("application/pdf").contentLength(204800L).build()
        ));

        // When
        noticeService.createNotice(req);

        // Then
        assertThat(savedNotice.getAttachments()).hasSize(2);
        assertThat(savedNotice.getAttachments())
                .extracting(NoticeAttachment::getAttachmentType)
                .containsExactlyInAnyOrder(AttachmentType.IMAGE, AttachmentType.FILE);
    }

    @Test
    void createNotice_withInvalidImageKey_throwsBadRequest() {
        // Given
        List<String> badKeys = List.of("images/projects/wrong.png"); // NOTICE 카테고리 아님

        CreateNoticeRequest req = mock(CreateNoticeRequest.class);
        when(req.getImageStoredFileNames()).thenReturn(badKeys);
        // getFileStoredFileNames는 IMAGE 검증이 먼저 예외를 던지므로 호출되지 않음 → stub 생략

        doThrow(new BadRequestException(null, "storedFileName 규칙 위반"))
                .when(fileUploadService).validateStoredFileNames(badKeys, UploadCategory.NOTICE, FileStorageType.IMAGE);

        // When & Then
        assertThatThrownBy(() -> noticeService.createNotice(req))
                .isInstanceOf(BadRequestException.class);

        verify(noticeRepository, never()).save(any());
    }

    @Test
    void createNotice_originalFileNameFallsBackToKey_whenUploadedFileNotFound() {
        // Given
        List<String> imageKeys = List.of(IMG_KEY_1);

        CreateNoticeRequest req = mock(CreateNoticeRequest.class);
        when(req.getImageStoredFileNames()).thenReturn(imageKeys);
        when(req.getFileStoredFileNames()).thenReturn(List.of());
        when(req.toEntity()).thenReturn(Notice.builder().title("공지").content("내용").build());

        Notice savedNotice = Notice.builder().title("공지").content("내용").build();
        when(noticeRepository.save(any())).thenReturn(savedNotice);

        // UploadedFile 조회 결과 없음 → originalFileName이 key로 폴백
        when(fileUploadService.findUploadedFiles(imageKeys)).thenReturn(List.of());

        // When
        noticeService.createNotice(req);

        // Then
        assertThat(savedNotice.getAttachments()).hasSize(1);
        assertThat(savedNotice.getAttachments().get(0).getOriginalFileName()).isEqualTo(IMG_KEY_1);
    }

    // ── getNoticeDetail ───────────────────────────────────────────────────────

    @Test
    void getNoticeDetail_withImageAndFileAttachments_buildsUrlsCorrectly() {
        // Given
        Notice notice = Notice.builder().title("공지제목").content("내용").pinned(false).build();

        NoticeAttachment imgAttachment = NoticeAttachment.builder()
                .notice(notice).attachmentType(AttachmentType.IMAGE)
                .storedFileName(IMG_KEY_1).originalFileName("banner.png").build();
        NoticeAttachment fileAttachment = NoticeAttachment.builder()
                .notice(notice).attachmentType(AttachmentType.FILE)
                .storedFileName(FILE_KEY_1).originalFileName("report.pdf").build();
        notice.addAttachment(imgAttachment);
        notice.addAttachment(fileAttachment);

        when(noticeRepository.findByIdWithAttachments(1L)).thenReturn(Optional.of(notice));
        when(fileUploadService.buildFileUrl(IMG_KEY_1)).thenReturn(IMG_URL_1);
        when(fileUploadService.buildFileUrl(FILE_KEY_1)).thenReturn(FILE_URL_1);

        // When
        NoticeDetailResponse response = noticeService.getNoticeDetail(1L);

        // Then
        assertAll(
                () -> assertThat(response.getTitle()).isEqualTo("공지제목"),
                () -> assertThat(response.getImages()).hasSize(1),
                () -> assertThat(response.getImages().get(0).getFileUrl()).isEqualTo(IMG_URL_1),
                () -> assertThat(response.getImages().get(0).getOriginalFileName()).isEqualTo("banner.png"),
                () -> assertThat(response.getFiles()).hasSize(1),
                () -> assertThat(response.getFiles().get(0).getFileUrl()).isEqualTo(FILE_URL_1),
                () -> assertThat(response.getFiles().get(0).getOriginalFileName()).isEqualTo("report.pdf")
        );
    }

    @Test
    void getNoticeDetail_withNoAttachments_returnsEmptyLists() {
        // Given
        Notice notice = Notice.builder().title("첨부없음").content("내용").build();
        when(noticeRepository.findByIdWithAttachments(1L)).thenReturn(Optional.of(notice));

        // When
        NoticeDetailResponse response = noticeService.getNoticeDetail(1L);

        // Then
        assertThat(response.getImages()).isEmpty();
        assertThat(response.getFiles()).isEmpty();
        verifyNoInteractions(fileUploadService);
    }

    @Test
    void getNoticeDetail_multipleImages_allUrlsBuilt() {
        // Given
        Notice notice = Notice.builder().title("공지").content("내용").build();

        NoticeAttachment img1 = NoticeAttachment.builder()
                .notice(notice).attachmentType(AttachmentType.IMAGE)
                .storedFileName(IMG_KEY_1).originalFileName("banner.png").build();
        NoticeAttachment img2 = NoticeAttachment.builder()
                .notice(notice).attachmentType(AttachmentType.IMAGE)
                .storedFileName(IMG_KEY_2).originalFileName("thumb.png").build();
        notice.addAttachment(img1);
        notice.addAttachment(img2);

        when(noticeRepository.findByIdWithAttachments(1L)).thenReturn(Optional.of(notice));
        when(fileUploadService.buildFileUrl(IMG_KEY_1)).thenReturn(IMG_URL_1);
        when(fileUploadService.buildFileUrl(IMG_KEY_2)).thenReturn(IMG_URL_2);

        // When
        NoticeDetailResponse response = noticeService.getNoticeDetail(1L);

        // Then
        assertThat(response.getImages()).hasSize(2);
        assertThat(response.getFiles()).isEmpty();
        assertThat(response.getImages())
                .extracting("fileUrl")
                .containsExactlyInAnyOrder(IMG_URL_1, IMG_URL_2);
    }

    @Test
    void getNoticeDetail_notFound_throwsNotFoundException() {
        // Given
        when(noticeRepository.findByIdWithAttachments(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> noticeService.getNoticeDetail(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(NoticeErrorCode.NOT_FOUND.getMessage());
    }

    // ── updateNotice ──────────────────────────────────────────────────────────

    @Test
    void updateNotice_updatesTextFields_whenAllProvided() {
        // Given
        Notice notice = Notice.builder().title("원래제목").content("원래내용").pinned(false).build();
        when(noticeRepository.findByIdWithAttachments(1L)).thenReturn(Optional.of(notice));

        UpdateNoticeRequest req = mock(UpdateNoticeRequest.class);
        when(req.getTitle()).thenReturn("새제목");
        when(req.getContent()).thenReturn("새내용");
        when(req.getPinned()).thenReturn(true);
        when(req.getImageStoredFileNames()).thenReturn(List.of());
        when(req.getFileStoredFileNames()).thenReturn(List.of());
        when(req.getAttachmentIdsToDelete()).thenReturn(List.of());

        // When
        noticeService.updateNotice(1L, req);

        // Then
        assertAll(
                () -> assertThat(notice.getTitle()).isEqualTo("새제목"),
                () -> assertThat(notice.getContent()).isEqualTo("새내용"),
                () -> assertThat(notice.getPinned()).isTrue()
        );
    }

    @Test
    void updateNotice_addsNewImageAttachments() {
        // Given
        Notice notice = Notice.builder().title("공지").content("내용").build();
        when(noticeRepository.findByIdWithAttachments(1L)).thenReturn(Optional.of(notice));

        List<String> newImageKeys = List.of(IMG_KEY_1);
        UpdateNoticeRequest req = mock(UpdateNoticeRequest.class);
        when(req.getImageStoredFileNames()).thenReturn(newImageKeys);
        when(req.getFileStoredFileNames()).thenReturn(List.of());
        when(req.getAttachmentIdsToDelete()).thenReturn(List.of());
        when(req.getTitle()).thenReturn(null);
        when(req.getContent()).thenReturn(null);
        when(req.getPinned()).thenReturn(null);

        when(fileUploadService.findUploadedFiles(newImageKeys)).thenReturn(List.of(
                UploadedFile.builder().uploadCategory(UploadCategory.NOTICE)
                        .storedFileName(IMG_KEY_1).originalFileName("banner.png")
                        .contentType("image/png").contentLength(1024L).build()
        ));

        // When
        noticeService.updateNotice(1L, req);

        // Then
        verify(fileUploadService).validateStoredFileNames(newImageKeys, UploadCategory.NOTICE, FileStorageType.IMAGE);
        assertThat(notice.getAttachments()).hasSize(1);
        assertThat(notice.getAttachments().get(0).getAttachmentType()).isEqualTo(AttachmentType.IMAGE);
    }

    @Test
    void updateNotice_deletesSpecifiedAttachments_andCallsS3Delete() {
        // Given
        Notice notice = Notice.builder().title("공지").content("내용").build();
        NoticeAttachment attachment = NoticeAttachment.builder()
                .notice(notice).attachmentType(AttachmentType.IMAGE)
                .storedFileName(IMG_KEY_1).originalFileName("banner.png").build();

        // 리플렉션으로 id 설정
        setId(attachment, 10L);
        notice.addAttachment(attachment);

        when(noticeRepository.findByIdWithAttachments(1L)).thenReturn(Optional.of(notice));

        UpdateNoticeRequest req = mock(UpdateNoticeRequest.class);
        when(req.getImageStoredFileNames()).thenReturn(List.of());
        when(req.getFileStoredFileNames()).thenReturn(List.of());
        when(req.getAttachmentIdsToDelete()).thenReturn(List.of(10L));
        when(req.getTitle()).thenReturn(null);
        when(req.getContent()).thenReturn(null);
        when(req.getPinned()).thenReturn(null);

        // When
        noticeService.updateNotice(1L, req);

        // Then
        verify(fileUploadService).deleteFile(IMG_KEY_1);
        assertThat(notice.getAttachments()).isEmpty();
    }

    @Test
    void updateNotice_deleteIdNotInAttachments_doesNothing() {
        // Given
        Notice notice = Notice.builder().title("공지").content("내용").build();
        NoticeAttachment attachment = NoticeAttachment.builder()
                .notice(notice).attachmentType(AttachmentType.IMAGE)
                .storedFileName(IMG_KEY_1).originalFileName("banner.png").build();
        setId(attachment, 10L);
        notice.addAttachment(attachment);

        when(noticeRepository.findByIdWithAttachments(1L)).thenReturn(Optional.of(notice));

        UpdateNoticeRequest req = mock(UpdateNoticeRequest.class);
        when(req.getImageStoredFileNames()).thenReturn(List.of());
        when(req.getFileStoredFileNames()).thenReturn(List.of());
        when(req.getAttachmentIdsToDelete()).thenReturn(List.of(999L)); // 없는 ID
        when(req.getTitle()).thenReturn(null);
        when(req.getContent()).thenReturn(null);
        when(req.getPinned()).thenReturn(null);

        // When
        noticeService.updateNotice(1L, req);

        // Then
        verify(fileUploadService, never()).deleteFile(any());
        assertThat(notice.getAttachments()).hasSize(1); // 삭제 안 됨
    }

    @Test
    void updateNotice_nullFieldsNotOverwritten() {
        // Given - title/content/pinned 모두 null이면 update() 미호출
        Notice notice = spy(Notice.builder().title("원래제목").content("원래내용").pinned(false).build());
        when(noticeRepository.findByIdWithAttachments(1L)).thenReturn(Optional.of(notice));

        UpdateNoticeRequest req = mock(UpdateNoticeRequest.class);
        when(req.getTitle()).thenReturn(null);
        when(req.getContent()).thenReturn(null);
        when(req.getPinned()).thenReturn(null);
        when(req.getImageStoredFileNames()).thenReturn(List.of());
        when(req.getFileStoredFileNames()).thenReturn(List.of());
        when(req.getAttachmentIdsToDelete()).thenReturn(List.of());

        // When
        noticeService.updateNotice(1L, req);

        // Then — update() 메서드 자체가 호출되지 않음
        verify(notice, never()).update(any(), any(), any());
    }

    @Test
    void updateNotice_notFound_throwsNotFoundException() {
        // Given
        when(noticeRepository.findByIdWithAttachments(99L)).thenReturn(Optional.empty());
        UpdateNoticeRequest req = mock(UpdateNoticeRequest.class);

        // When & Then
        assertThatThrownBy(() -> noticeService.updateNotice(99L, req))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(NoticeErrorCode.NOT_FOUND.getMessage());
    }

    // ── deleteNotice ──────────────────────────────────────────────────────────

    @Test
    void deleteNotice_deletesAllAttachmentFilesFromS3ThenDeletesNotice() {
        // Given
        Notice notice = Notice.builder().title("공지").content("내용").build();
        NoticeAttachment img = NoticeAttachment.builder()
                .notice(notice).attachmentType(AttachmentType.IMAGE)
                .storedFileName(IMG_KEY_1).originalFileName("banner.png").build();
        NoticeAttachment file = NoticeAttachment.builder()
                .notice(notice).attachmentType(AttachmentType.FILE)
                .storedFileName(FILE_KEY_1).originalFileName("report.pdf").build();
        notice.addAttachment(img);
        notice.addAttachment(file);

        when(noticeRepository.findByIdWithAttachments(1L)).thenReturn(Optional.of(notice));

        // When
        noticeService.deleteNotice(1L);

        // Then
        verify(fileUploadService).deleteFile(IMG_KEY_1);
        verify(fileUploadService).deleteFile(FILE_KEY_1);
        verify(noticeRepository).delete(notice);
    }

    @Test
    void deleteNotice_withNoAttachments_deletesNoticeWithoutS3Call() {
        // Given
        Notice notice = Notice.builder().title("공지").content("내용").build();
        when(noticeRepository.findByIdWithAttachments(1L)).thenReturn(Optional.of(notice));

        // When
        noticeService.deleteNotice(1L);

        // Then
        verify(fileUploadService, never()).deleteFile(any());
        verify(noticeRepository).delete(notice);
    }

    @Test
    void deleteNotice_notFound_throwsNotFoundException() {
        // Given
        when(noticeRepository.findByIdWithAttachments(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> noticeService.deleteNotice(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(NoticeErrorCode.NOT_FOUND.getMessage());
    }

    // ── removeAttachment ──────────────────────────────────────────────────────

    @Test
    void removeAttachment_validAttachment_deletesS3FileAndRecord() {
        // Given
        Long noticeId = 1L;
        Long attachmentId = 10L;

        Notice notice = mock(Notice.class);
        when(notice.getId()).thenReturn(noticeId);

        NoticeAttachment attachment = mock(NoticeAttachment.class);
        when(attachment.getNotice()).thenReturn(notice);
        when(attachment.getStoredFileName()).thenReturn(IMG_KEY_1);

        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.of(attachment));

        // When
        noticeService.removeAttachment(noticeId, attachmentId);

        // Then
        verify(fileUploadService).deleteFile(IMG_KEY_1);
        verify(attachmentRepository).delete(attachment);
    }

    @Test
    void removeAttachment_attachmentBelongsToDifferentNotice_throwsNotFound() {
        // Given
        Long noticeId = 1L;
        Long attachmentId = 10L;

        Notice otherNotice = mock(Notice.class);
        when(otherNotice.getId()).thenReturn(999L); // 다른 notice

        NoticeAttachment attachment = mock(NoticeAttachment.class);
        when(attachment.getNotice()).thenReturn(otherNotice);

        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.of(attachment));

        // When & Then
        assertThatThrownBy(() -> noticeService.removeAttachment(noticeId, attachmentId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(NoticeErrorCode.ATTACHMENT_NOT_FOUND.getMessage());

        verify(fileUploadService, never()).deleteFile(any());
    }

    @Test
    void removeAttachment_notFound_throwsNotFoundException() {
        // Given
        when(attachmentRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> noticeService.removeAttachment(1L, 99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(NoticeErrorCode.ATTACHMENT_NOT_FOUND.getMessage());
    }

    // ── getNoticePage ─────────────────────────────────────────────────────────

    @Test
    void getNoticePage_withoutKeyword_usesDefaultRepository() {
        // Given
        Notice notice = Notice.builder().title("공지1").content("내용1").pinned(false).build();
        var page = new PageImpl<>(List.of(notice), PageRequest.of(0, 10), 1);
        when(noticeRepository.findAllByOrderByPinnedDescUpdatedAtDesc(any())).thenReturn(page);

        // When
        var result = noticeService.getNoticePage(0, 10, null);

        // Then
        verify(noticeRepository).findAllByOrderByPinnedDescUpdatedAtDesc(any());
        verify(noticeRepository, never())
                .findByTitleContainingIgnoreCaseOrderByPinnedDescUpdatedAtDesc(any(), any());
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getNoticePage_withBlankKeyword_usesDefaultRepository() {
        // Given
        var page = new PageImpl<Notice>(List.of(), PageRequest.of(0, 10), 0);
        when(noticeRepository.findAllByOrderByPinnedDescUpdatedAtDesc(any())).thenReturn(page);

        // When
        noticeService.getNoticePage(0, 10, "   ");

        // Then
        verify(noticeRepository).findAllByOrderByPinnedDescUpdatedAtDesc(any());
        verify(noticeRepository, never())
                .findByTitleContainingIgnoreCaseOrderByPinnedDescUpdatedAtDesc(any(), any());
    }

    @Test
    void getNoticePage_withKeyword_usesKeywordRepository() {
        // Given
        Notice notice = Notice.builder().title("멋사공지").content("내용").pinned(true).build();
        var page = new PageImpl<>(List.of(notice), PageRequest.of(0, 10), 1);
        when(noticeRepository.findByTitleContainingIgnoreCaseOrderByPinnedDescUpdatedAtDesc(eq("멋사"), any()))
                .thenReturn(page);

        // When
        var result = noticeService.getNoticePage(0, 10, "멋사");

        // Then
        verify(noticeRepository).findByTitleContainingIgnoreCaseOrderByPinnedDescUpdatedAtDesc(eq("멋사"), any());
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("멋사공지");
    }

    // ── 리플렉션 유틸 ─────────────────────────────────────────────────────────

    private void setId(Object entity, Long id) {
        try {
            // NoticeAttachment → BaseEntity 에 id 필드 있음
            var field = entity.getClass().getSuperclass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException("ID 설정 실패", e);
        }
    }
}
