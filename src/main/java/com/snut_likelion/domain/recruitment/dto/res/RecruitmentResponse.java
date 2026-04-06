package com.snut_likelion.domain.recruitment.dto.res;

import com.snut_likelion.domain.recruitment.entity.Recruitment;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecruitmentResponse {

    private Long id;
    private int generation;
    private String recruitmentType;
    private LocalDateTime openDate;
    private LocalDateTime closeDate;

    @Builder
    public RecruitmentResponse(Long id, int generation, String recruitmentType, LocalDateTime openDate, LocalDateTime closeDate) {
        this.id = id;
        this.generation = generation;
        this.recruitmentType = recruitmentType;
        this.openDate = openDate;
        this.closeDate = closeDate;
    }

    public static RecruitmentResponse from(Recruitment recruitment) {
        return RecruitmentResponse.builder()
                .id(recruitment.getId())
                .generation(recruitment.getGeneration())
                .recruitmentType(recruitment.getRecruitmentType().name())
                .openDate(recruitment.getOpenDate())
                .closeDate(recruitment.getCloseDate())
                .build();
    }
}
