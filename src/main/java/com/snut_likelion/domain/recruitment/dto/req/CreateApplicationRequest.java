package com.snut_likelion.domain.recruitment.dto.req;

import com.snut_likelion.domain.recruitment.entity.Application;
import com.snut_likelion.domain.recruitment.entity.ApplicationStatus;
import com.snut_likelion.domain.recruitment.entity.DepartmentType;
import com.snut_likelion.domain.user.entity.Part;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "지원서 작성 요청")
public class CreateApplicationRequest {

    @ArraySchema(
            schema = @Schema(implementation = ApplicationAnswerRequest.class),
            arraySchema = @Schema(
                    description = "질문 답변 목록. GET /recruitments/{recId}/questions?part={part}&department={department} 에서 조회한 모든 질문에 대해 답변해야 합니다. " +
                            "COMMON 질문 + 선택한 PART의 질문 + (운영진 모집 시) 선택한 DEPARTMENT의 질문 모두 포함해야 합니다."
            )
    )
    private List<ApplicationAnswerRequest> answers;

    @Schema(description = "개인정보 수집 동의 여부", example = "true")
    @NotNull(message = "개인정보 수집 동의 여부를 입력해주세요.")
    private Boolean isPersonalInfoConsent;

    @Schema(description = "포트폴리오 링크 (선택사항)", example = "https://www.notion.com/portfolio", nullable = true)
    private String portfolio;

    @Schema(description = "지원 파트", example = "PLANNING", allowableValues = {"PLANNING", "DESIGN", "FRONTEND", "BACKEND"})
    @NotNull(message = "지원 파트를 선택해주세요.")
    private Part part;

    @Schema(description = "지원 부서 (운영진 모집 시 필수)", example = "OPERATION", nullable = true, allowableValues = {"OPERATION", "MEDIA", "EDU", "TECH"})
    private DepartmentType departmentType;

    @Builder
    public CreateApplicationRequest(List<ApplicationAnswerRequest> answers, Boolean isPersonalInfoConsent, String portfolio, Part part, DepartmentType departmentType) {
        this.answers = answers;
        this.isPersonalInfoConsent = isPersonalInfoConsent;
        this.portfolio = portfolio;
        this.part = part;
        this.departmentType = departmentType;
    }

    public Application toEntity() {
        return Application.builder()
                .isPersonalInfoConsent(isPersonalInfoConsent)
                .portfolio(portfolio)
                .part(part)
                .departmentType(departmentType)
                .status(ApplicationStatus.DRAFT)
                .build();
    }
}
