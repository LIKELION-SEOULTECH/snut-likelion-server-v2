package com.snut_likelion.domain.recruitment.entity;

import com.snut_likelion.domain.user.entity.Part;
import com.snut_likelion.global.common.support.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
//@Table(
//        name = "question",
//        uniqueConstraints = {
//                @UniqueConstraint(
//                        name = "UK_QUESTION_ORDER_RECRUITMENT",
//                        columnNames = {"orderNum", "recruitment_id"}
//                )
//        }
//)
public class Question extends BaseEntity {

    @Column(nullable = false)
    private String text;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private QuestionTarget questionTarget;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private QuestionType questionType;

    @Enumerated(EnumType.STRING)
    private Part part;  // 파트 질문일 경우

    @Enumerated(EnumType.STRING)
    private DepartmentType departmentType; // 부서 질문일 경우

    @Column(nullable = false)
    private int orderNum;

    private String buttonList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruitment_id", referencedColumnName = "id")
    private Recruitment recruitment; // 해당 질문이 속한 모집 공고

    @Builder
    public Question(Long id, String text, QuestionTarget questionTarget, QuestionType questionType, Part part, DepartmentType departmentType, int orderNum, String buttonList) {
        this.id = id;
        this.text = text;
        this.questionTarget = questionTarget;
        this.questionType = questionType;
        this.part = part;
        this.departmentType = departmentType;
        this.orderNum = orderNum;
        this.buttonList = buttonList;
    }

    public List<String> getButtonList() {
        if (buttonList == null || buttonList.isEmpty()) {
            return new ArrayList<>();
        }

        return List.of(buttonList.split(", "));
    }

    /**
     * === 연관관계 메서드 ===
     */
    public void setRecruitment(Recruitment recruitment) {
        this.recruitment = recruitment;
    }
}
