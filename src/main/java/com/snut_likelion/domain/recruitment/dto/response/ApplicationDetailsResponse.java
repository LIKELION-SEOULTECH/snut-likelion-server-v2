package com.snut_likelion.domain.recruitment.dto.response;

import com.snut_likelion.domain.recruitment.entity.Answer;
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
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApplicationDetailsResponse {

    private Long id;
    private String username;
    private String major;
    private Boolean inSchool;
    private String studentId;
    private String phoneNumber;
    private int grade;
    private Boolean isPersonalInfoConsent;
    private String portfolioName;
    private String part; // 지원 파트
    private String departmentType; // 부서 (운영진 지원일 경우)
    private String status; // 지원 상태 (대기, 합격, 불합격 등)
    private List<ApplicationAnswerResponse> answers;
    private LocalDateTime submittedAt; // 지원서 제출 시간

    @Builder
    public ApplicationDetailsResponse(Long id, String username, String major, Boolean inSchool, String studentId, String phoneNumber, int grade, Boolean isPersonalInfoConsent, String portfolioName, Part part, DepartmentType departmentType, ApplicationStatus status, List<Answer> answers, LocalDateTime submittedAt) {
        this.id = id;
        this.username = username;
        this.major = major;
        this.inSchool = inSchool;
        this.studentId = studentId;
        this.phoneNumber = phoneNumber;
        this.grade = grade;
        this.isPersonalInfoConsent = isPersonalInfoConsent;
        this.portfolioName = portfolioName;
        this.part = part.name();
        this.departmentType = departmentType != null ? departmentType.name() : null;
        this.status = status.name();
        this.answers = answers.stream().map(ApplicationAnswerResponse::from).collect(Collectors.toList());
        this.submittedAt = submittedAt;
    }

    public static ApplicationDetailsResponse from(Application application) {
        return ApplicationDetailsResponse.builder()
                .id(application.getId())
                .username(application.getUser().getUsername())
                .major(application.getMajor())
                .phoneNumber(application.getUser().getPhoneNumber())
                .inSchool(application.getInSchool())
                .studentId(application.getStudentId())
                .grade(application.getGrade())
                .isPersonalInfoConsent(application.getIsPersonalInfoConsent())
                .portfolioName(application.getPortfolio())
                .part(application.getPart())
                .departmentType(application.getDepartmentType())
                .status(application.getStatus())
                .answers(application.getAnswers())
                .submittedAt(application.getSubmittedAt())
                .build();
    }
}
