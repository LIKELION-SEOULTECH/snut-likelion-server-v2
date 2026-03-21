package com.snut_likelion.domain.notice.dto.res;

import com.snut_likelion.domain.notice.entity.Notice;
import com.snut_likelion.domain.notice.entity.NoticeAttachment;
import com.snut_likelion.domain.notice.entity.AttachmentType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@Builder
public class NoticeDetailResponse {

    private Long noticeId;
    private String title;
    private String content;
    private String summary;
    private LocalDateTime updatedAt;

    /** 이미지 첨부 목록 */
    private List<NoticeFileDto> images;

    /** 일반 파일 첨부 목록 */
    private List<NoticeFileDto> files;

    /**
     * 첨부파일 URL 맵을 받아 DTO를 생성한다.
     *
     * @param notice       공지사항 엔티티 (attachments fetch 완료 상태)
     * @param urlMap       storedFileName → 접근 URL 맵
     * @param originalNameMap storedFileName → originalFileName 맵 (UploadedFile에서 조회)
     */
    public static NoticeDetailResponse of(Notice notice,
                                          Map<String, String> urlMap,
                                          Map<String, String> originalNameMap) {
        List<NoticeFileDto> images = notice.getAttachments().stream()
                .filter(a -> a.getAttachmentType() == AttachmentType.IMAGE)
                .map(a -> NoticeFileDto.of(a, urlMap.getOrDefault(a.getStoredFileName(), "")))
                .toList();

        List<NoticeFileDto> files = notice.getAttachments().stream()
                .filter(a -> a.getAttachmentType() == AttachmentType.FILE)
                .map(a -> NoticeFileDto.of(a, urlMap.getOrDefault(a.getStoredFileName(), "")))
                .toList();

        return NoticeDetailResponse.builder()
                .noticeId(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .summary(notice.getSummary())
                .updatedAt(notice.getUpdatedAt())
                .images(images)
                .files(files)
                .build();
    }
}