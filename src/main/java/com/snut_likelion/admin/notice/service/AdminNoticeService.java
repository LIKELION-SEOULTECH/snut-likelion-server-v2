package com.snut_likelion.admin.notice.service;

import com.snut_likelion.admin.notice.dto.res.NoticePageResponse;
import com.snut_likelion.admin.notice.infra.AdminNoticeQueryRepository;
import com.snut_likelion.domain.notice.dto.req.CreateNoticeRequest;
import com.snut_likelion.domain.notice.dto.req.UpdateNoticeRequest;
import com.snut_likelion.domain.notice.entity.Notice;
import com.snut_likelion.domain.notice.exception.NoticeErrorCode;
import com.snut_likelion.domain.notice.repository.NoticeRepository;
import com.snut_likelion.domain.notice.service.NoticeService;
import com.snut_likelion.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminNoticeService {

    private final int PAGE_SIZE = 8;

    private final AdminNoticeQueryRepository queryRepository;
    private final NoticeService              noticeService;
    private final NoticeRepository           noticeRepository;

    // 목록 페이지
    @Transactional(readOnly = true)
    public NoticePageResponse getNoticeList(int page, String keyword) {
        PageRequest pageRequest = PageRequest.of(page, PAGE_SIZE);
        Page<NoticePageResponse.NoticeListResponse> result =
                queryRepository.getNoticeList(keyword, pageRequest);

        return NoticePageResponse.from(result);
    }

    @Transactional
    public void create(CreateNoticeRequest req) { noticeService.createNotice(req); }

    @Transactional
    public void modify(Long noticeId, UpdateNoticeRequest req) {
        noticeService.updateNotice(noticeId, req);
    }

    @Transactional
    public void remove(Long noticeId) { noticeService.deleteNotice(noticeId); }

    @Transactional
    public void removeNotices(List<Long> ids) { ids.forEach(this::remove); }

    @Transactional
    public void removeAttachment(Long noticeId, Long attachmentId) {
        noticeService.removeAttachment(noticeId, attachmentId);
    }

    // 핀 토글
    @Transactional
    public void togglePin(Long noticeId) {
        Notice n = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new NotFoundException(NoticeErrorCode.NOT_FOUND));

        n.update(n.getTitle(), n.getContent(), !n.getPinned());
    }
}
