package com.snut_likelion.admin.recruitment.service;

import com.snut_likelion.admin.recruitment.dto.req.UpdateQuestionsRequest;
import com.snut_likelion.domain.recruitment.dto.res.QuestionResponse;
import com.snut_likelion.domain.recruitment.entity.*;
import com.snut_likelion.domain.recruitment.exception.QuestionErrorCode;
import com.snut_likelion.domain.recruitment.exception.RecruitmentErrorCode;
import com.snut_likelion.domain.recruitment.infra.RecruitmentRepository;
import com.snut_likelion.domain.user.entity.Part;
import com.snut_likelion.global.error.exception.BadRequestException;
import com.snut_likelion.global.error.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminQuestionServiceTest {

    @Mock
    RecruitmentRepository recruitmentRepository;

    @InjectMocks
    AdminQuestionService adminQuestionService;

    Recruitment memberRecruitment;
    Recruitment managerRecruitment;

    @BeforeEach
    void setup() {
        memberRecruitment = Recruitment.builder()
                .id(1L).generation(14).recruitmentType(RecruitmentType.MEMBER)
                .openDate(LocalDateTime.now()).closeDate(LocalDateTime.now().plusDays(7))
                .build();
        ReflectionTestUtils.setField(memberRecruitment, "questions", new ArrayList<>());
        ReflectionTestUtils.setField(memberRecruitment, "applications", new ArrayList<>());

        managerRecruitment = Recruitment.builder()
                .id(2L).generation(14).recruitmentType(RecruitmentType.MANAGER)
                .openDate(LocalDateTime.now()).closeDate(LocalDateTime.now().plusDays(7))
                .build();
        ReflectionTestUtils.setField(managerRecruitment, "questions", new ArrayList<>());
        ReflectionTestUtils.setField(managerRecruitment, "applications", new ArrayList<>());
    }

    @Test
    void getAllQuestions_success() {
        Question q1 = Question.builder().id(1L).text("q1").questionTarget(QuestionTarget.COMMON)
                .questionType(QuestionType.SHORT).orderNum(1).build();
        Question q2 = Question.builder().id(2L).text("q2").questionTarget(QuestionTarget.COMMON)
                .questionType(QuestionType.SHORT).orderNum(2).build();
        memberRecruitment.setQuestions(List.of(q2, q1));

        when(recruitmentRepository.findWithQuestionsById(1L)).thenReturn(Optional.of(memberRecruitment));

        List<QuestionResponse> result = adminQuestionService.getAllQuestions(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getOrderNum()).isLessThanOrEqualTo(result.get(1).getOrderNum());
    }

    @Test
    void getAllQuestions_notFound_throws() {
        when(recruitmentRepository.findWithQuestionsById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminQuestionService.getAllQuestions(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(RecruitmentErrorCode.NOT_FOUND_RECRUITMENT.getMessage());
    }

    @Test
    void updateQuestions_success() {
        when(recruitmentRepository.findById(1L)).thenReturn(Optional.of(memberRecruitment));

        List<UpdateQuestionsRequest> req = List.of(
                UpdateQuestionsRequest.builder().text("q1").questionTarget(QuestionTarget.COMMON)
                        .questionType(QuestionType.SHORT).order(1).build()
        );

        adminQuestionService.updateQuestions(1L, req);
        assertThat(memberRecruitment.getQuestions()).hasSize(1);
    }

    @Test
    void updateQuestions_memberWithDepartmentQuestion_throws() {
        when(recruitmentRepository.findById(1L)).thenReturn(Optional.of(memberRecruitment));

        List<UpdateQuestionsRequest> req = List.of(
                UpdateQuestionsRequest.builder().text("q1").questionTarget(QuestionTarget.DEPARTMENT)
                        .questionType(QuestionType.SHORT).order(1).build()
        );

        assertThatThrownBy(() -> adminQuestionService.updateQuestions(1L, req))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(QuestionErrorCode.INVALID_QUESTION_INCLUDE.getMessage());
    }

    @Test
    void updateQuestions_managerWithDepartmentQuestion_success() {
        when(recruitmentRepository.findById(2L)).thenReturn(Optional.of(managerRecruitment));

        List<UpdateQuestionsRequest> req = List.of(
                UpdateQuestionsRequest.builder().text("q1").questionTarget(QuestionTarget.DEPARTMENT)
                        .questionType(QuestionType.SHORT).order(1).build()
        );

        adminQuestionService.updateQuestions(2L, req);
        assertThat(managerRecruitment.getQuestions()).hasSize(1);
    }

    @Test
    void updateQuestions_notFound_throws() {
        when(recruitmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminQuestionService.updateQuestions(99L, List.of()))
                .isInstanceOf(NotFoundException.class);
    }
}
