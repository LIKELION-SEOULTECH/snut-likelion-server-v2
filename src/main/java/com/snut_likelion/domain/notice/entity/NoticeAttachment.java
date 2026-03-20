package com.snut_likelion.domain.notice.entity;

import com.snut_likelion.global.common.support.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notice_attachment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoticeAttachment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id", nullable = false)
    private Notice notice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AttachmentType attachmentType;

    /**
     * S3 key (storedFileName)
     * ex) images/notices/uuid-filename.png
     *     files/notices/uuid-filename.pdf
     */
    @Column(nullable = false, length = 500)
    private String storedFileName;

    /**
     * 클라이언트가 업로드한 원본 파일명
     */
    @Column(nullable = false, length = 255)
    private String originalFileName;

    @Builder
    private NoticeAttachment(Notice notice, AttachmentType attachmentType,
                             String storedFileName, String originalFileName) {
        this.notice = notice;
        this.attachmentType = attachmentType;
        this.storedFileName = storedFileName;
        this.originalFileName = originalFileName;
    }
}
