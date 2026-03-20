package com.snut_likelion.domain.recruitment.service;

import com.snut_likelion.domain.recruitment.dto.response.QuestionResponse;
import com.snut_likelion.domain.recruitment.entity.*;
import com.snut_likelion.domain.recruitment.exception.RecruitmentErrorCode;
import com.snut_likelion.domain.recruitment.infra.QuestionFilter;
import com.snut_likelion.domain.recruitment.infra.RecruitmentRepository;
import com.snut_likelion.domain.user.entity.Part;
import com.snut_likelion.global.error.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @Mock
    private RecruitmentRepository recruitmentRepository;

    @Mock
    private QuestionFilter questionFilter;

    @InjectMocks
    private QuestionService questionService;

    private Question createQuestion(Long id, QuestionTarget target, Part part, DepartmentType departmentType) {
        return Question.builder()
                .id(id)
                .text("q" + id)
                .questionTarget(target)
                .questionType(QuestionType.LONG)
                .part(part)
                .departmentType(departmentType)
                .orderNum(id.intValue())
                .build();
    }

    @Test
    void getQuestions_member_returnsDefaultCommonAndPart() {
        // Given
        Long recId = 1L;
        Recruitment recruitment = Recruitment.builder()
                .id(recId)
                .generation(13)
                .recruitmentType(RecruitmentType.MEMBER)
                .openDate(LocalDateTime.of(2025, 6, 22, 0, 0))
                .closeDate(LocalDateTime.of(2025, 6, 29, 23, 59))
                .build();

        Question dq1 = createQuestion(1L, QuestionTarget.DEFAULT, null, null);
        Question dq2 = createQuestion(2L, QuestionTarget.DEFAULT, null, null);
        Question cq1 = createQuestion(3L, QuestionTarget.COMMON, null, null);
        Question pq1 = createQuestion(4L, QuestionTarget.PART, Part.BACKEND, null);

        when(recruitmentRepository.findWithQuestionsById(recId)).thenReturn(Optional.of(recruitment));
        when(questionFilter.getRequiredQuestions(recruitment, Part.BACKEND, null))
                .thenReturn(List.of(dq1, dq2, cq1, pq1));

        // When
        List<QuestionResponse> result = questionService.getQuestions(recId, Part.BACKEND, null);

        // Then
        assertThat(result).hasSize(4)
                .extracting(QuestionResponse::getId, QuestionResponse::getQuestionTarget)
                .containsExactly(
                        tuple(1L, "DEFAULT"),
                        tuple(2L, "DEFAULT"),
                        tuple(3L, "COMMON"),
                        tuple(4L, "PART")
                );
    }

    @Test
    void getQuestions_manager_returnsDefaultCommonPartAndDepartment() {
        // Given
        Long recId = 1L;
        Recruitment recruitment = Recruitment.builder()
                .id(recId)
                .generation(13)
                .recruitmentType(RecruitmentType.MANAGER)
                .openDate(LocalDateTime.of(2025, 6, 22, 0, 0))
                .closeDate(LocalDateTime.of(2025, 6, 29, 23, 59))
                .build();

        Question dq1 = createQuestion(1L, QuestionTarget.DEFAULT, null, null);
        Question cq1 = createQuestion(2L, QuestionTarget.COMMON, null, null);
        Question pq1 = createQuestion(3L, QuestionTarget.PART, Part.FRONTEND, null);
        Question dept1 = createQuestion(4L, QuestionTarget.DEPARTMENT, null, DepartmentType.OPERATION);

        when(recruitmentRepository.findWithQuestionsById(recId)).thenReturn(Optional.of(recruitment));
        when(questionFilter.getRequiredQuestions(recruitment, Part.FRONTEND, DepartmentType.OPERATION))
                .thenReturn(List.of(dq1, cq1, pq1, dept1));

        // When
        List<QuestionResponse> result = questionService.getQuestions(recId, Part.FRONTEND, DepartmentType.OPERATION);

        // Then
        assertThat(result).hasSize(4)
                .extracting(QuestionResponse::getId, QuestionResponse::getQuestionTarget)
                .containsExactly(
                        tuple(1L, "DEFAULT"),
                        tuple(2L, "COMMON"),
                        tuple(3L, "PART"),
                        tuple(4L, "DEPARTMENT")
                );
    }

    @Test
    void getQuestions_notFoundRecruitment_throws() {
        // Given
        Long recId = 999L;
        when(recruitmentRepository.findWithQuestionsById(recId)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> questionService.getQuestions(recId, Part.BACKEND, null))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(RecruitmentErrorCode.NOT_FOUND_RECRUITMENT.getMessage());
    }
}
