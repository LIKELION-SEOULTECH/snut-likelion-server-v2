package com.snut_likelion.admin.notice.service;

import com.snut_likelion.admin.notice.dto.res.NoticePageResponse;
import com.snut_likelion.admin.notice.infra.AdminNoticeQueryRepository;
import com.snut_likelion.domain.notice.entity.Notice;
import com.snut_likelion.domain.notice.exception.NoticeErrorCode;
import com.snut_likelion.domain.notice.repository.NoticeRepository;
import com.snut_likelion.domain.notice.service.NoticeService;
import com.snut_likelion.global.error.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminNoticeServiceTest {

    @Mock
    AdminNoticeQueryRepository queryRepository;

    @Mock
    NoticeService noticeService;

    @Mock
    NoticeRepository noticeRepository;

    @InjectMocks
    AdminNoticeService adminNoticeService;

    @Test
    void create_delegatesToNoticeService() {
        adminNoticeService.create(null);
        verify(noticeService).createNotice(null);
    }

    @Test
    void modify_delegatesToNoticeService() {
        adminNoticeService.modify(1L, null);
        verify(noticeService).updateNotice(1L, null);
    }

    @Test
    void remove_delegatesToNoticeService() {
        adminNoticeService.remove(1L);
        verify(noticeService).deleteNotice(1L);
    }

    @Test
    void removeNotices_callsRemoveForEach() {
        adminNoticeService.removeNotices(List.of(1L, 2L, 3L));
        verify(noticeService, times(3)).deleteNotice(org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void removeAttachment_delegatesToNoticeService() {
        adminNoticeService.removeAttachment(1L, 2L);
        verify(noticeService).removeAttachment(1L, 2L);
    }

    @Test
    void togglePin_pinned_toUnpinned() {
        Notice notice = Notice.builder().title("공지").content("내용").pinned(true).build();
        when(noticeRepository.findById(1L)).thenReturn(Optional.of(notice));

        adminNoticeService.togglePin(1L);

        assertThat(notice.getPinned()).isFalse();
    }

    @Test
    void togglePin_unpinned_toPinned() {
        Notice notice = Notice.builder().title("공지").content("내용").pinned(false).build();
        when(noticeRepository.findById(1L)).thenReturn(Optional.of(notice));

        adminNoticeService.togglePin(1L);

        assertThat(notice.getPinned()).isTrue();
    }

    @Test
    void togglePin_notFound_throws() {
        when(noticeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminNoticeService.togglePin(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(NoticeErrorCode.NOT_FOUND.getMessage());
    }

    @Test
    void getNoticeList_returnsPagedResponse() {
        Page<NoticePageResponse.NoticeListResponse> page = new PageImpl<>(List.of(), PageRequest.of(0, 8), 0);
        when(queryRepository.getNoticeList(eq("공지"), any(PageRequest.class))).thenReturn(page);

        NoticePageResponse response = adminNoticeService.getNoticeList(0, "공지");

        assertThat(response).isNotNull();
        assertThat(response.getTotalElements()).isZero();
    }
}
