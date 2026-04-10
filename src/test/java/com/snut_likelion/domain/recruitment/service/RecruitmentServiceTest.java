package com.snut_likelion.domain.recruitment.service;

import com.snut_likelion.domain.recruitment.dto.res.RecruitmentResponse;
import com.snut_likelion.domain.recruitment.entity.Recruitment;
import com.snut_likelion.domain.recruitment.entity.RecruitmentType;
import com.snut_likelion.domain.recruitment.exception.RecruitmentErrorCode;
import com.snut_likelion.domain.recruitment.infra.RecruitmentRepository;
import com.snut_likelion.global.error.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecruitmentServiceTest {

    @Mock
    RecruitmentRepository recruitmentRepository;

    @InjectMocks
    RecruitmentService recruitmentService;

    @Test
    void getRecentRecruitmentInfo_success() {
        Recruitment recruitment = Recruitment.builder()
                .id(1L)
                .generation(14)
                .recruitmentType(RecruitmentType.MEMBER)
                .openDate(LocalDateTime.now().minusDays(1))
                .closeDate(LocalDateTime.now().plusDays(7))
                .build();

        when(recruitmentRepository.findRecentRecruitment(RecruitmentType.MEMBER))
                .thenReturn(Optional.of(recruitment));

        RecruitmentResponse result = recruitmentService.getRecentRecruitmentInfo(RecruitmentType.MEMBER);

        assertThat(result).isNotNull();
        assertThat(result.getGeneration()).isEqualTo(14);
    }

    @Test
    void getRecentRecruitmentInfo_notFound_throws() {
        when(recruitmentRepository.findRecentRecruitment(RecruitmentType.MEMBER))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> recruitmentService.getRecentRecruitmentInfo(RecruitmentType.MEMBER))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(RecruitmentErrorCode.NOT_FOUND_RECRUITMENT.getMessage());
    }
}
