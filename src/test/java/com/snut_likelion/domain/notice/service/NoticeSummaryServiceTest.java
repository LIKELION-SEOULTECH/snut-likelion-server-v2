package com.snut_likelion.domain.notice.service;

import com.snut_likelion.domain.notice.entity.Notice;
import com.snut_likelion.domain.notice.exception.NoticeErrorCode;
import com.snut_likelion.domain.notice.repository.NoticeRepository;
import com.snut_likelion.domain.notice.repository.SummaryApiClient;
import com.snut_likelion.global.error.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoticeSummaryServiceTest {

    @Mock
    NoticeRepository noticeRepository;

    @Mock
    SummaryApiClient summaryApiClient;

    @InjectMocks
    NoticeSummaryService noticeSummaryService;

    @Test
    void generateAndSaveSummary_success() {
        Notice notice = Notice.builder().title("공지").content("내용").pinned(false).build();
        when(summaryApiClient.summarize("내용")).thenReturn("요약 결과");
        when(noticeRepository.findById(1L)).thenReturn(Optional.of(notice));

        noticeSummaryService.generateAndSaveSummary(1L, "내용");

        assertThat(notice.getSummary()).isEqualTo("요약 결과");
    }

    @Test
    void generateAndSaveSummary_exceptionSwallowed() {
        // 예외 발생해도 공지 등록에 영향 없도록 무시
        when(summaryApiClient.summarize("내용")).thenThrow(new RuntimeException("AI 서버 오류"));

        noticeSummaryService.generateAndSaveSummary(1L, "내용");

        // 예외 없이 통과해야 함
        verify(summaryApiClient).summarize("내용");
    }

    @Test
    void generateAndSaveSummary_noticeNotFound_exceptionSwallowed() {
        when(summaryApiClient.summarize("내용")).thenReturn("요약");
        when(noticeRepository.findById(99L)).thenReturn(Optional.empty());

        // NotFoundException도 내부에서 catch되어 무시됨
        noticeSummaryService.generateAndSaveSummary(99L, "내용");

        verify(noticeRepository).findById(99L);
    }
}
