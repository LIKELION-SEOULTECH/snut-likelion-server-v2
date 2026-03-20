package com.snut_likelion.domain.notice.repository;

import com.snut_likelion.domain.notice.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    // 검색어 O, 고정순, 최신순(업데이트 기준)
    Page<Notice> findByTitleContainingIgnoreCaseOrderByPinnedDescUpdatedAtDesc(
            String keyword, Pageable pageable);

    // 검색어 X, 고정순, 최신순(업데이트 기준)
    Page<Notice> findAllByOrderByPinnedDescUpdatedAtDesc(Pageable pageable);

    // 첨부파일 포함 단건 조회 (N+1 방지)
    @Query("SELECT n FROM Notice n LEFT JOIN FETCH n.attachments WHERE n.id = :id")
    Optional<Notice> findByIdWithAttachments(@Param("id") Long id);
}