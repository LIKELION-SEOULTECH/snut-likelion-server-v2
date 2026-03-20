package com.snut_likelion.admin.recruitment.dto.req;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.snut_likelion.domain.recruitment.entity.DepartmentType;
import com.snut_likelion.domain.recruitment.entity.Question;
import com.snut_likelion.domain.recruitment.entity.QuestionTarget;
import com.snut_likelion.domain.recruitment.entity.QuestionType;
import com.snut_likelion.domain.user.entity.Part;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateQuestionsRequest {

    private Long id; // 기존 질문이면 id 값 포함

    @NotEmpty(message = "질문 내용은 비어있을 수 없습니다.")
    private String text;

    @NotNull(message = "질문 대상(범위)은 필수입니다.")
    private QuestionTarget questionTarget;

    @NotNull(message = "질문 유형은 필수입니다.")
    private QuestionType questionType;

    @NotNull(message = "순서를 함께 전달해주세요")
    private Integer order;

    // PART 대상인 경우에만 사용
    private Part part;

    // DEPARTMENT 대상인 경우에만 사용
    private DepartmentType departmentType;

    // RADIO_BUTTON 타입인 경우에만 사용
    private List<String> buttonList;

    @Builder
    public UpdateQuestionsRequest(Long id, String text, QuestionTarget questionTarget, QuestionType questionType, Integer order, Part part, DepartmentType departmentType, List<String> buttonList) {
        this.id = id;
        this.text = text;
        this.questionTarget = questionTarget;
        this.questionType = questionType;
        this.order = order;
        this.part = part;
        this.departmentType = departmentType;
        this.buttonList = buttonList;
    }

    @AssertTrue
    public boolean isValid() {
        // 질문 유형이 PART인 경우, part가 null이 아니어야 함
        if (questionTarget == QuestionTarget.PART) {
            return part != null;
        }
        // 질문 유형이 DEPARTMENT인 경우, departmentType이 null이 아니어야 함
        if (questionTarget == QuestionTarget.DEPARTMENT) {
            return departmentType != null;
        }
        // COMMON 질문 유형은 추가 조건 없음

        if (questionType == QuestionType.RADIO_BUTTON) {
            return buttonList != null;
        }

        return true;
    }

    public Question toEntity() {
        return Question.builder()
                .id(id)
                .text(text)
                .questionTarget(questionTarget)
                .questionType(questionType)
                .orderNum(order)
                .part(part)
                .departmentType(departmentType)
                .buttonList(buttonList == null ? null : String.join(", ", buttonList))
                .build();
    }

}
