package com.snut_likelion.domain.recruitment.infra;

import com.snut_likelion.domain.recruitment.entity.*;
import com.snut_likelion.domain.user.entity.Part;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class QuestionFilterTest {

    private final QuestionFilter questionFilter = new QuestionFilter();

    @Test
    void member_returns_default_common_and_matchingPart() {
        // Given
        Question defaultQ = createQuestion(1L, QuestionTarget.DEFAULT, null, null);
        Question commonQ = createQuestion(2L, QuestionTarget.COMMON, null, null);
        Question backendQ = createQuestion(3L, QuestionTarget.PART, Part.BACKEND, null);
        Question frontendQ = createQuestion(4L, QuestionTarget.PART, Part.FRONTEND, null);

        Recruitment recruitment = mock(Recruitment.class);
        when(recruitment.getRecruitmentType()).thenReturn(RecruitmentType.MEMBER);
        when(recruitment.getQuestions()).thenReturn(List.of(defaultQ, commonQ, backendQ, frontendQ));

        // When
        List<Question> result = questionFilter.getRequiredQuestions(recruitment, Part.BACKEND, null);

        // Then
        assertThat(result).containsExactlyInAnyOrder(defaultQ, commonQ, backendQ);
    }

    @Test
    void manager_returns_default_common_matchingPart_and_matchingDepartment() {
        // Given
        Question defaultQ = createQuestion(1L, QuestionTarget.DEFAULT, null, null);
        Question commonQ = createQuestion(2L, QuestionTarget.COMMON, null, null);
        Question backendQ = createQuestion(3L, QuestionTarget.PART, Part.BACKEND, null);
        Question deptQ = createQuestion(4L, QuestionTarget.DEPARTMENT, null, DepartmentType.OPERATION);
        Question otherDeptQ = createQuestion(5L, QuestionTarget.DEPARTMENT, null, DepartmentType.MARKETING);

        Recruitment recruitment = mock(Recruitment.class);
        when(recruitment.getRecruitmentType()).thenReturn(RecruitmentType.MANAGER);
        when(recruitment.getQuestions()).thenReturn(List.of(defaultQ, commonQ, backendQ, deptQ, otherDeptQ));

        // When
        List<Question> result = questionFilter.getRequiredQuestions(recruitment, Part.BACKEND, DepartmentType.OPERATION);

        // Then
        assertThat(result).containsExactlyInAnyOrder(defaultQ, commonQ, backendQ, deptQ);
    }

    @Test
    void default_questions_always_included_regardless_of_part() {
        // Given
        Question defaultQ1 = createQuestion(1L, QuestionTarget.DEFAULT, null, null);
        Question defaultQ2 = createQuestion(2L, QuestionTarget.DEFAULT, null, null);
        Question designQ = createQuestion(3L, QuestionTarget.PART, Part.DESIGN, null);
        Question backendQ = createQuestion(4L, QuestionTarget.PART, Part.BACKEND, null);

        Recruitment recruitment = mock(Recruitment.class);
        when(recruitment.getRecruitmentType()).thenReturn(RecruitmentType.MEMBER);
        when(recruitment.getQuestions()).thenReturn(List.of(defaultQ1, defaultQ2, designQ, backendQ));

        // When
        List<Question> result = questionFilter.getRequiredQuestions(recruitment, Part.DESIGN, null);

        // Then
        assertThat(result)
                .containsExactlyInAnyOrder(defaultQ1, defaultQ2, designQ)
                .doesNotContain(backendQ);
    }

    private Question createQuestion(Long id, QuestionTarget target, Part part, DepartmentType deptType) {
        return Question.builder()
                .id(id)
                .text("q" + id)
                .questionTarget(target)
                .part(part)
                .departmentType(deptType)
                .build();
    }
}
