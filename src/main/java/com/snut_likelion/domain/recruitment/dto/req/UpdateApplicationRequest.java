package com.snut_likelion.domain.recruitment.dto.req;

import com.snut_likelion.domain.recruitment.entity.DepartmentType;
import com.snut_likelion.domain.user.entity.Part;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UpdateApplicationRequest {

    private List<ApplicationAnswerRequest> answers;

    @NotNull(message = "개인정보 수집 동의 여부를 입력해주세요.")
    private Boolean isPersonalInfoConsent;

    private String portfolio;

    @NotNull(message = "지원 파트를 선택해주세요.")
    private Part part;

    private DepartmentType departmentType;

    @Builder
    public UpdateApplicationRequest(List<ApplicationAnswerRequest> answers, Boolean isPersonalInfoConsent, String portfolio, Part part, DepartmentType departmentType) {
        this.answers = answers;
        this.isPersonalInfoConsent = isPersonalInfoConsent;
        this.portfolio = portfolio;
        this.part = part;
        this.departmentType = departmentType;
    }
}
