package com.snut_likelion.admin.recruitment.service;

import com.snut_likelion.admin.recruitment.dto.req.CreateRecruitmentRequest;
import com.snut_likelion.admin.recruitment.dto.req.UpdateRecruitmentRequest;
import com.snut_likelion.domain.recruitment.entity.Recruitment;
import com.snut_likelion.domain.recruitment.entity.RecruitmentType;
import com.snut_likelion.domain.recruitment.exception.RecruitmentErrorCode;
import com.snut_likelion.domain.recruitment.infra.RecruitmentRepository;
import com.snut_likelion.global.error.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminRecruitmentServiceTest {

    @Mock
    RecruitmentRepository recruitmentRepository;

    @InjectMocks
    AdminRecruitmentService adminRecruitmentService;

    @Test
    void createRecruitment_success() {
        CreateRecruitmentRequest req = CreateRecruitmentRequest.builder()
                .generation(14)
                .recruitmentType("MEMBER")
                .openDate(LocalDateTime.of(2026, 3, 1, 0, 0))
                .closeDate(LocalDateTime.of(2026, 3, 7, 23, 59))
                .build();

        adminRecruitmentService.createRecruitment(req);

        ArgumentCaptor<Recruitment> captor = ArgumentCaptor.forClass(Recruitment.class);
        verify(recruitmentRepository).save(captor.capture());
        assertThat(captor.getValue().getGeneration()).isEqualTo(14);
    }

    @Test
    void updateRecruitment_success() {
        Recruitment recruitment = Recruitment.builder()
                .id(1L).generation(13).recruitmentType(RecruitmentType.MEMBER)
                .openDate(LocalDateTime.now()).closeDate(LocalDateTime.now().plusDays(7))
                .build();
        when(recruitmentRepository.findById(1L)).thenReturn(Optional.of(recruitment));

        UpdateRecruitmentRequest req = UpdateRecruitmentRequest.builder()
                .generation(14)
                .recruitmentType("MEMBER")
                .openDate(LocalDateTime.of(2026, 4, 1, 0, 0))
                .closeDate(LocalDateTime.of(2026, 4, 7, 23, 59))
                .build();

        adminRecruitmentService.updateRecruitment(1L, req);

        assertThat(recruitment.getGeneration()).isEqualTo(14);
    }

    @Test
    void updateRecruitment_notFound_throws() {
        when(recruitmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminRecruitmentService.updateRecruitment(99L,
                UpdateRecruitmentRequest.builder().generation(14).recruitmentType("MEMBER")
                        .openDate(LocalDateTime.now()).closeDate(LocalDateTime.now().plusDays(7)).build()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(RecruitmentErrorCode.NOT_FOUND_RECRUITMENT.getMessage());
    }

    @Test
    void removeRecruitment_success() {
        Recruitment recruitment = Recruitment.builder().id(1L).generation(14)
                .recruitmentType(RecruitmentType.MEMBER)
                .openDate(LocalDateTime.now()).closeDate(LocalDateTime.now().plusDays(7)).build();
        when(recruitmentRepository.findById(1L)).thenReturn(Optional.of(recruitment));

        adminRecruitmentService.removeRecruitment(1L);

        verify(recruitmentRepository).delete(recruitment);
    }

    @Test
    void removeRecruitment_notFound_throws() {
        when(recruitmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminRecruitmentService.removeRecruitment(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(RecruitmentErrorCode.NOT_FOUND_RECRUITMENT.getMessage());
    }
}
