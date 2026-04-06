package com.snut_likelion.domain.recruitment.dto.res;

import com.snut_likelion.domain.recruitment.entity.Application;
import com.snut_likelion.domain.recruitment.entity.ApplicationStatus;
import com.snut_likelion.domain.recruitment.entity.DepartmentType;
import com.snut_likelion.domain.user.entity.Part;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApplicationDetailsResponse {

    private Long id;
    private Boolean isPersonalInfoConsent;
    private String portfolio;
    private String part; // 지원 파트
    private String departmentType; // 부서 (운영진 지원일 경우)
    private String status; // 지원 상태 (대기, 합격, 불합격 등)
    private List<ApplicationAnswerResponse> answers;
    private LocalDateTime submittedAt; // 지원서 제출 시간

    @Builder
    public ApplicationDetailsResponse(Long id, Boolean isPersonalInfoConsent, String portfolio, Part part, DepartmentType departmentType, ApplicationStatus status, List<ApplicationAnswerResponse> answers, LocalDateTime submittedAt) {
        this.id = id;
        this.isPersonalInfoConsent = isPersonalInfoConsent;
        this.portfolio = portfolio;
        this.part = part.name();
        this.departmentType = departmentType != null ? departmentType.name() : null;
        this.status = status.name();
        this.answers = answers;
        this.submittedAt = submittedAt;
    }

    public static ApplicationDetailsResponse from(Application application) {
        List<ApplicationAnswerResponse> answers = application.getAnswers().stream()
                .map(ApplicationAnswerResponse::from)
                .toList();

        return ApplicationDetailsResponse.builder()
                .id(application.getId())
                .isPersonalInfoConsent(application.getIsPersonalInfoConsent())
                .portfolio(application.getPortfolio())
                .part(application.getPart())
                .departmentType(application.getDepartmentType())
                .status(application.getStatus())
                .answers(answers)
                .submittedAt(application.getSubmittedAt())
                .build();
    }
}
