package com.snut_likelion.domain.recruitment.infra;

import com.snut_likelion.domain.recruitment.entity.DepartmentType;
import com.snut_likelion.domain.recruitment.entity.Question;
import com.snut_likelion.domain.recruitment.entity.QuestionTarget;
import com.snut_likelion.domain.recruitment.entity.Recruitment;
import com.snut_likelion.domain.user.entity.Part;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class QuestionFilter {

    public List<Question> getRequiredQuestions(Recruitment recruitment, Part part, DepartmentType department) {
        List<Question> allQuestions = recruitment.getQuestions();
        return allQuestions.stream()
                .filter(q -> {
                    // 기본 질문, 공통 질문은 무조건
                    if (q.getQuestionTarget() == QuestionTarget.DEFAULT
                            || q.getQuestionTarget() == QuestionTarget.COMMON) {
                        return true;
                    }

                    switch (recruitment.getRecruitmentType()) {
                        // 회원 모집인 경우: PART 질문만, 그 중에서도 req.getPart 와 같은 것
                        case MEMBER -> {
                            return q.getQuestionTarget() == QuestionTarget.PART
                                    && q.getPart() == part;
                        }
                        // 운영진 모집인 경우: PART + DEPARTMENT 둘 다
                        case MANAGER -> {
                            // PART 질문 중, req.getPart 와 같은 것
                            if (q.getQuestionTarget() == QuestionTarget.PART) {
                                return q.getPart() == part;
                            }
                            // DEPARTMENT 질문 중, req.getDepartmentType 와 같은 것
                            if (q.getQuestionTarget() == QuestionTarget.DEPARTMENT) {
                                return q.getDepartmentType() == department;
                            }
                        }
                    }

                    return false;
                })
                .toList();
    }
}
