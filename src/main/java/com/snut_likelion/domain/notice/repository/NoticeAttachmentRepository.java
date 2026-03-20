package com.snut_likelion.domain.notice.repository;

import com.snut_likelion.domain.notice.entity.NoticeAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoticeAttachmentRepository extends JpaRepository<NoticeAttachment, Long> {

    List<NoticeAttachment> findAllByNoticeId(Long noticeId);
}
