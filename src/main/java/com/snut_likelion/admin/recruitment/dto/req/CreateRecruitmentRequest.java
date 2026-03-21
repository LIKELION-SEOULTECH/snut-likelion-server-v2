package com.snut_likelion.admin.recruitment.dto.req;

import com.snut_likelion.domain.recruitment.entity.Recruitment;
import com.snut_likelion.domain.recruitment.entity.RecruitmentType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreateRecruitmentRequest {

    @NotNull(message = "기수를 입력해주세요.")
    private Integer generation;

    @NotEmpty(message = "모집 유형을 입력해주세요.")
    private String recruitmentType;

    @NotNull(message = "모집 시작 시각을 입력해주세요.")
    private LocalDateTime openDate;

    @NotNull(message = "모집 종료 시각을 입력해주세요.")
    private LocalDateTime closeDate;

    @Builder
    public CreateRecruitmentRequest(Integer generation, String recruitmentType, LocalDateTime openDate, LocalDateTime closeDate) {
        this.generation = generation;
        this.recruitmentType = recruitmentType;
        this.openDate = openDate;
        this.closeDate = closeDate;
    }

    public Recruitment toEntity() {
        return Recruitment.builder()
                .generation(generation)
                .recruitmentType(RecruitmentType.valueOf(recruitmentType))
                .openDate(openDate)
                .closeDate(closeDate)
                .build();
    }
}
