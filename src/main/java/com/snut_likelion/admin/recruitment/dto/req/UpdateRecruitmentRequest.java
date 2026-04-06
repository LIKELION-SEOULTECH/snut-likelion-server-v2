package com.snut_likelion.admin.recruitment.dto.req;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UpdateRecruitmentRequest {

    private Integer generation;
    private String recruitmentType;
    private LocalDateTime openDate;
    private LocalDateTime closeDate;

    @Builder
    public UpdateRecruitmentRequest(Integer generation, String recruitmentType, LocalDateTime openDate, LocalDateTime closeDate) {
        this.generation = generation;
        this.recruitmentType = recruitmentType;
        this.openDate = openDate;
        this.closeDate = closeDate;
    }
}
