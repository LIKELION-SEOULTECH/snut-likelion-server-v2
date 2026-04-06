package com.snut_likelion.domain.recruitment.dto.req;

import com.snut_likelion.domain.recruitment.entity.Answer;
import com.snut_likelion.domain.recruitment.entity.Application;
import com.snut_likelion.domain.recruitment.entity.Question;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "지원서 답변 요청")
public class ApplicationAnswerRequest {

    @Schema(description = "질문 ID (GET /recruitments/{recId}/questions 에서 조회한 질문의 id)", example = "1", required = true)
    private Long questionId;

    @Schema(description = "해당 질문에 대한 답변", example = "홍길동", required = true)
    private String answer;

    @Builder
    public ApplicationAnswerRequest(Long questionId, String answer) {
        this.questionId = questionId;
        this.answer = answer;
    }

    public Answer toEntity(Question question, Application application) {
        return Answer.builder()
                .question(question)
                .application(application)
                .text(answer)
                .build();
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
