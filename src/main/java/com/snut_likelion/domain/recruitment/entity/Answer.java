package com.snut_likelion.domain.recruitment.entity;

import com.snut_likelion.global.common.support.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Answer extends BaseEntity {

    @Column(nullable = false, length = 500)
    private String text; // 답변 내용

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", referencedColumnName = "id")
    private Question question; // 해당 답변이 속한 질문

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", referencedColumnName = "id")
    private Application application; // 해당 답변이 속한 지원서

    @Builder
    public Answer(Long id, String text, Question question, Application application) {
        this.id = id;
        this.text = text;
        this.question = question;
        this.application = application;
    }

    /**
     * === 연관관계 메서드 ===
     */

    public void setQuestion(Question question) {
        this.question = question;
    }

    public void setApplication(Application application) {
        this.application = application;
    }
}
