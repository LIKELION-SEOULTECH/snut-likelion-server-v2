package com.snut_likelion.domain.recruitment.service;

import com.snut_likelion.domain.recruitment.dto.request.ApplicationAnswerRequest;
import com.snut_likelion.domain.recruitment.dto.request.CreateApplicationRequest;
import com.snut_likelion.domain.recruitment.dto.request.UpdateApplicationRequest;
import com.snut_likelion.domain.recruitment.entity.*;
import com.snut_likelion.domain.recruitment.exception.ApplicationErrorCode;
import com.snut_likelion.domain.recruitment.exception.RecruitmentErrorCode;
import com.snut_likelion.domain.recruitment.infra.ApplicationRepository;
import com.snut_likelion.domain.recruitment.infra.QuestionFilter;
import com.snut_likelion.domain.recruitment.infra.QuestionRepository;
import com.snut_likelion.domain.recruitment.infra.RecruitmentRepository;
import com.snut_likelion.domain.user.entity.Part;
import com.snut_likelion.domain.user.entity.User;
import com.snut_likelion.domain.user.exception.UserErrorCode;
import com.snut_likelion.domain.user.repository.UserRepository;
import com.snut_likelion.global.auth.model.UserInfo;
import com.snut_likelion.global.error.exception.BadRequestException;
import com.snut_likelion.global.error.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationCommandServiceTest {

    @Mock
    ApplicationRepository applicationRepository;

    @Mock
    RecruitmentRepository recruitmentRepository;

    @Mock
    QuestionRepository questionRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    QuestionFilter questionFilter;

    @InjectMocks
    ApplicationCommandService service;

    Long recId = 1L, userId = 1L, appId = 1L;
    Recruitment recruitment;
    User user;
    Question q1, q2, q3, q4, q5, q6;

    @BeforeEach
    void setup() {
        recruitment = Recruitment.builder()
                .id(recId)
                .generation(13)
                .recruitmentType(RecruitmentType.MEMBER)
                .openDate(LocalDateTime.of(2025, 6, 22, 0, 0))
                .closeDate(LocalDateTime.of(2025, 6, 29, 23, 59))
                .build();
        user = User.builder()
                .id(userId)
                .username("tester")
                .email("test@example.com")
                .build();

        q1 = this.createQuestion(1L, QuestionTarget.COMMON, null, null);
        q2 = this.createQuestion(2L, QuestionTarget.COMMON, null, null);
        q3 = this.createQuestion(3L, QuestionTarget.PART, Part.BACKEND, null);
        q4 = this.createQuestion(4L, QuestionTarget.PART, Part.BACKEND, null);
        q5 = this.createQuestion(5L, QuestionTarget.DEPARTMENT, null, DepartmentType.OPERATION);
        q6 = this.createQuestion(6L, QuestionTarget.DEPARTMENT, null, DepartmentType.OPERATION);

    }

    private Question createQuestion(Long id, QuestionTarget type, Part part, DepartmentType departmentType) {
        return Question.builder()
                .id(id)
                .text("q" + id)
                .questionTarget(type)
                .part(part)
                .departmentType(departmentType)
                .build();
    }

    @Test
    void createApplication_draft() {
        ApplicationAnswerRequest ar1 = createAnsReq(1L);
        ApplicationAnswerRequest ar2 = createAnsReq(2L);
        ApplicationAnswerRequest ar3 = createAnsReq(3L);
        ApplicationAnswerRequest ar4 = createAnsReq(4L);

        CreateApplicationRequest req = CreateApplicationRequest.builder()
                .major("공학")
                .studentId("20201234")
                .grade(3)
                .inSchool(true)
                .isPersonalInfoConsent(true)
                .part(Part.BACKEND)
                .departmentType(null)
                .portfolio("https://example.com/portfolio.pdf")
                .answers(List.of(ar1, ar2, ar3, ar4))
                .build();

        when(recruitmentRepository.findById(recId)).thenReturn(Optional.of(recruitment));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(questionRepository.findById(q1.getId())).thenReturn(Optional.of(q1));
        when(questionRepository.findById(q2.getId())).thenReturn(Optional.of(q2));
        when(questionRepository.findById(q3.getId())).thenReturn(Optional.of(q3));
        when(questionRepository.findById(q4.getId())).thenReturn(Optional.of(q4));

        // when
        service.createApplication(recId, userId, false, req);

        // then
        ArgumentCaptor<Application> captor = ArgumentCaptor.forClass(Application.class);
        verify(applicationRepository).save(captor.capture());
        Application saved = captor.getValue();

        assertAll(
                () -> assertThat(saved.getUser()).isEqualTo(user),
                () -> assertThat(saved.getMajor()).isEqualTo("공학"),
                () -> assertThat(saved.getInSchool()).isTrue(),
                () -> assertThat(saved.getStudentId()).isEqualTo("20201234"),
                () -> assertThat(saved.getGrade()).isEqualTo(3),
                () -> assertThat(saved.getIsPersonalInfoConsent()).isTrue(),
                () -> assertThat(saved.getPortfolio()).isEqualTo("https://example.com/portfolio.pdf"),
                () -> assertThat(saved.getPart()).isEqualTo(Part.BACKEND),
                () -> assertThat(saved.getDepartmentType()).isNull(),
                () -> assertThat(saved.getStatus()).isEqualTo(ApplicationStatus.DRAFT),
                () -> assertThat(saved.getSubmittedAt()).isNotNull(),
                () -> assertThat(saved.getAnswers()).hasSize(4)
                        .extracting(Answer::getQuestion)
                        .containsExactlyInAnyOrder(q1, q2, q3, q4)
        );

    }

    @Test
    void createApplication_submit() {
        ApplicationAnswerRequest ar1 = createAnsReq(1L);
        ApplicationAnswerRequest ar2 = createAnsReq(2L);
        ApplicationAnswerRequest ar3 = createAnsReq(3L);
        ApplicationAnswerRequest ar4 = createAnsReq(4L);

        CreateApplicationRequest req = CreateApplicationRequest.builder()
                .major("공학")
                .studentId("20201234")
                .grade(3)
                .inSchool(true)
                .isPersonalInfoConsent(true)
                .part(Part.BACKEND)
                .departmentType(null)
                .portfolio("https://example.com/portfolio.pdf")
                .answers(List.of(ar1, ar2, ar3, ar4))
                .build();

        when(recruitmentRepository.findById(recId)).thenReturn(Optional.of(recruitment));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(questionRepository.findById(q1.getId())).thenReturn(Optional.of(q1));
        when(questionRepository.findById(q2.getId())).thenReturn(Optional.of(q2));
        when(questionRepository.findById(q3.getId())).thenReturn(Optional.of(q3));
        when(questionRepository.findById(q4.getId())).thenReturn(Optional.of(q4));
        when(questionFilter.getRequiredQuestions(recruitment, req.getPart(), req.getDepartmentType())).thenReturn(List.of(q1, q2, q3, q4));
        // when
        service.createApplication(recId, userId, true, req);

        // then
        ArgumentCaptor<Application> captor = ArgumentCaptor.forClass(Application.class);
        verify(applicationRepository).save(captor.capture());
        Application saved = captor.getValue();

        assertAll(
                () -> assertThat(saved.getUser()).isEqualTo(user),
                () -> assertThat(saved.getMajor()).isEqualTo("공학"),
                () -> assertThat(saved.getInSchool()).isTrue(),
                () -> assertThat(saved.getStudentId()).isEqualTo("20201234"),
                () -> assertThat(saved.getGrade()).isEqualTo(3),
                () -> assertThat(saved.getIsPersonalInfoConsent()).isTrue(),
                () -> assertThat(saved.getPortfolio()).isEqualTo("https://example.com/portfolio.pdf"),
                () -> assertThat(saved.getPart()).isEqualTo(Part.BACKEND),
                () -> assertThat(saved.getDepartmentType()).isNull(),
                () -> assertThat(saved.getStatus()).isEqualTo(ApplicationStatus.SUBMITTED),
                () -> assertThat(saved.getSubmittedAt()).isNotNull(),
                () -> assertThat(saved.getAnswers()).hasSize(4)
                        .extracting(Answer::getQuestion)
                        .containsExactlyInAnyOrder(q1, q2, q3, q4)
        );
    }

    @Test
    void createApplication_submit_manager() {
        recruitment = Recruitment.builder()
                .id(recId)
                .generation(13)
                .recruitmentType(RecruitmentType.MANAGER)
                .openDate(LocalDateTime.of(2025, 6, 22, 0, 0))
                .closeDate(LocalDateTime.of(2025, 6, 29, 23, 59))
                .build();

        ApplicationAnswerRequest ar1 = createAnsReq(1L);
        ApplicationAnswerRequest ar2 = createAnsReq(2L);
        ApplicationAnswerRequest ar3 = createAnsReq(3L);
        ApplicationAnswerRequest ar4 = createAnsReq(4L);
        ApplicationAnswerRequest ar5 = createAnsReq(5L);
        ApplicationAnswerRequest ar6 = createAnsReq(6L);

        CreateApplicationRequest req = CreateApplicationRequest.builder()
                .major("공학")
                .studentId("20201234")
                .grade(3)
                .inSchool(true)
                .isPersonalInfoConsent(true)
                .part(Part.FRONTEND)
                .departmentType(DepartmentType.OPERATION)
                .portfolio("https://example.com/portfolio.pdf")
                .answers(List.of(ar1, ar2, ar3, ar4, ar5, ar6))
                .build();

        when(recruitmentRepository.findById(recId)).thenReturn(Optional.of(recruitment));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(questionRepository.findById(q1.getId())).thenReturn(Optional.of(q1));
        when(questionRepository.findById(q2.getId())).thenReturn(Optional.of(q2));
        when(questionRepository.findById(q3.getId())).thenReturn(Optional.of(q3));
        when(questionRepository.findById(q4.getId())).thenReturn(Optional.of(q4));
        when(questionRepository.findById(q5.getId())).thenReturn(Optional.of(q5));
        when(questionRepository.findById(q6.getId())).thenReturn(Optional.of(q6));
        when(questionFilter.getRequiredQuestions(recruitment, req.getPart(), req.getDepartmentType())).thenReturn(List.of(q1, q2, q3, q4, q5, q6));
        // when
        service.createApplication(recId, userId, true, req);

        // then
        ArgumentCaptor<Application> captor = ArgumentCaptor.forClass(Application.class);
        verify(applicationRepository).save(captor.capture());
        Application saved = captor.getValue();

        assertAll(
                () -> assertThat(saved.getUser()).isEqualTo(user),
                () -> assertThat(saved.getMajor()).isEqualTo("공학"),
                () -> assertThat(saved.getInSchool()).isTrue(),
                () -> assertThat(saved.getStudentId()).isEqualTo("20201234"),
                () -> assertThat(saved.getGrade()).isEqualTo(3),
                () -> assertThat(saved.getIsPersonalInfoConsent()).isTrue(),
                () -> assertThat(saved.getPortfolio()).isEqualTo("https://example.com/portfolio.pdf"),
                () -> assertThat(saved.getPart()).isEqualTo(Part.FRONTEND),
                () -> assertThat(saved.getDepartmentType()).isEqualTo(DepartmentType.OPERATION),
                () -> assertThat(saved.getStatus()).isEqualTo(ApplicationStatus.SUBMITTED),
                () -> assertThat(saved.getSubmittedAt()).isNotNull(),
                () -> assertThat(saved.getAnswers()).hasSize(6)
                        .extracting(Answer::getQuestion)
                        .containsExactlyInAnyOrder(q1, q2, q3, q4, q5, q6)
        );
    }

    private ApplicationAnswerRequest createAnsReq(long questionId) {
        return ApplicationAnswerRequest.builder()
                .questionId(questionId)
                .answer("ans" + questionId)
                .build();
    }

    @Test
    void createApplication_invalidAnswerSet_throws() {
        // Given
        ApplicationAnswerRequest ar1 = createAnsReq(1L);
        ApplicationAnswerRequest ar2 = createAnsReq(2L);
        ApplicationAnswerRequest ar3 = createAnsReq(3L);

        CreateApplicationRequest req = CreateApplicationRequest.builder()
                .major("공학")
                .studentId("20201234")
                .grade(3)
                .inSchool(true)
                .isPersonalInfoConsent(true)
                .part(Part.BACKEND)
                .departmentType(null)
                .portfolio("https://example.com/portfolio.pdf")
                .answers(List.of(ar1, ar2, ar3))
                .build();

        when(recruitmentRepository.findById(recId)).thenReturn(Optional.of(recruitment));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(questionRepository.findById(q1.getId())).thenReturn(Optional.of(q1));
        when(questionRepository.findById(q2.getId())).thenReturn(Optional.of(q2));
        when(questionRepository.findById(q3.getId())).thenReturn(Optional.of(q3));
        when(questionFilter.getRequiredQuestions(recruitment, req.getPart(), req.getDepartmentType())).thenReturn(List.of(q1, q2, q3, q4));

        // When / Then
        assertThatThrownBy(() -> service.createApplication(recId, userId, true, req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining(ApplicationErrorCode.INVALID_ANSWER_SET.getMessage());

    }

    @Test
    void createApplication_missingRecruitment_throws() {
        // Given
        when(recruitmentRepository.findById(recId)).thenReturn(Optional.empty());
        CreateApplicationRequest req = CreateApplicationRequest.builder()
                .major("x")
                .studentId("y")
                .grade(1)
                .inSchool(true)
                .isPersonalInfoConsent(true)
                .part(Part.AI)
                .departmentType(null)
                .portfolio(null)
                .answers(List.of())
                .build();

        // When / Then
        assertThatThrownBy(() -> service.createApplication(recId, userId, false, req))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(RecruitmentErrorCode.NOT_FOUND_RECRUITMENT.getMessage());
    }

    @Test
    void createApplication_missingUser_throws() {
        // Given
        ApplicationAnswerRequest ar1 = createAnsReq(1L);
        when(recruitmentRepository.findById(recId)).thenReturn(Optional.of(recruitment));
        when(questionRepository.findById(q1.getId())).thenReturn(Optional.of(q1));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        CreateApplicationRequest req = CreateApplicationRequest.builder()
                .major("x")
                .studentId("y")
                .grade(1)
                .inSchool(true)
                .isPersonalInfoConsent(true)
                .part(Part.AI)
                .departmentType(null)
                .portfolio(null)
                .answers(List.of(ar1))
                .build();

        // When / Then
        assertThatThrownBy(() -> service.createApplication(recId, userId, false, req))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(UserErrorCode.NOT_FOUND.getMessage());
    }

    @Test
    void updateApplication_draft() {
        int currentGeneration = 13;
        Long appId = 1L;

        Application application = Application.builder().id(appId).build();

        ApplicationAnswerRequest ar1 = createAnsReq(1L);
        ApplicationAnswerRequest ar2 = createAnsReq(2L);
        ApplicationAnswerRequest ar3 = createAnsReq(3L);
        ApplicationAnswerRequest ar4 = createAnsReq(4L);

        UpdateApplicationRequest req = UpdateApplicationRequest.builder()
                .major("공학")
                .studentId("20201234")
                .grade(3)
                .inSchool(true)
                .isPersonalInfoConsent(true)
                .part(Part.BACKEND)
                .departmentType(null)
                .portfolio("https://example.com/portfolio.pdf")
                .answers(List.of(ar1, ar2, ar3, ar4))
                .build();

        when(applicationRepository.findById(appId)).thenReturn(Optional.of(application));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(questionRepository.findById(q1.getId())).thenReturn(Optional.of(q1));
        when(questionRepository.findById(q2.getId())).thenReturn(Optional.of(q2));
        when(questionRepository.findById(q3.getId())).thenReturn(Optional.of(q3));
        when(questionRepository.findById(q4.getId())).thenReturn(Optional.of(q4));

        UserInfo userInfo = UserInfo.from(user, currentGeneration);

        // when
        service.updateApplication(appId, userInfo, false, req);

        // then
        assertAll(
                () -> assertThat(application.getUser()).isEqualTo(user),
                () -> assertThat(application.getMajor()).isEqualTo("공학"),
                () -> assertThat(application.getInSchool()).isTrue(),
                () -> assertThat(application.getStudentId()).isEqualTo("20201234"),
                () -> assertThat(application.getGrade()).isEqualTo(3),
                () -> assertThat(application.getIsPersonalInfoConsent()).isTrue(),
                () -> assertThat(application.getPortfolio()).isEqualTo("https://example.com/portfolio.pdf"),
                () -> assertThat(application.getPart()).isEqualTo(Part.BACKEND),
                () -> assertThat(application.getDepartmentType()).isNull(),
                () -> assertThat(application.getStatus()).isEqualTo(ApplicationStatus.DRAFT),
                () -> assertThat(application.getSubmittedAt()).isNotNull(),
                () -> assertThat(application.getAnswers()).hasSize(4)
                        .extracting(Answer::getQuestion)
                        .containsExactlyInAnyOrder(q1, q2, q3, q4)
        );
    }

    @Test
    void updateApplication_submit() {
        int currentGeneration = 13;
        Long appId = 1L;

        Application application = Application.builder().id(appId).build();
        application.setRecruitment(recruitment);

        ApplicationAnswerRequest ar1 = createAnsReq(1L);
        ApplicationAnswerRequest ar2 = createAnsReq(2L);
        ApplicationAnswerRequest ar3 = createAnsReq(3L);
        ApplicationAnswerRequest ar4 = createAnsReq(4L);

        UpdateApplicationRequest req = UpdateApplicationRequest.builder()
                .major("공학")
                .studentId("20201234")
                .grade(3)
                .inSchool(true)
                .isPersonalInfoConsent(true)
                .part(Part.BACKEND)
                .departmentType(null)
                .portfolio("https://example.com/portfolio.pdf")
                .answers(List.of(ar1, ar2, ar3, ar4))
                .build();

        when(applicationRepository.findById(appId)).thenReturn(Optional.of(application));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(questionRepository.findById(q1.getId())).thenReturn(Optional.of(q1));
        when(questionRepository.findById(q2.getId())).thenReturn(Optional.of(q2));
        when(questionRepository.findById(q3.getId())).thenReturn(Optional.of(q3));
        when(questionRepository.findById(q4.getId())).thenReturn(Optional.of(q4));
        when(questionFilter.getRequiredQuestions(recruitment, req.getPart(), req.getDepartmentType())).thenReturn(List.of(q1, q2, q3, q4));

        UserInfo userInfo = UserInfo.from(user, currentGeneration);

        // when
        service.updateApplication(appId, userInfo, true, req);

        // then
        assertAll(
                () -> assertThat(application.getUser()).isEqualTo(user),
                () -> assertThat(application.getMajor()).isEqualTo("공학"),
                () -> assertThat(application.getInSchool()).isTrue(),
                () -> assertThat(application.getStudentId()).isEqualTo("20201234"),
                () -> assertThat(application.getGrade()).isEqualTo(3),
                () -> assertThat(application.getIsPersonalInfoConsent()).isTrue(),
                () -> assertThat(application.getPortfolio()).isEqualTo("https://example.com/portfolio.pdf"),
                () -> assertThat(application.getPart()).isEqualTo(Part.BACKEND),
                () -> assertThat(application.getDepartmentType()).isNull(),
                () -> assertThat(application.getStatus()).isEqualTo(ApplicationStatus.SUBMITTED),
                () -> assertThat(application.getSubmittedAt()).isNotNull(),
                () -> assertThat(application.getAnswers()).hasSize(4)
                        .extracting(Answer::getQuestion)
                        .containsExactlyInAnyOrder(q1, q2, q3, q4)
        );
    }

    @Test
    void createApplication_submit_withDefaultQuestions_allAnswered() {
        // Given: QuestionFilter가 DEFAULT + COMMON + PART 질문을 반환
        Question dq1 = createQuestion(10L, QuestionTarget.DEFAULT, null, null);
        Question dq2 = createQuestion(11L, QuestionTarget.DEFAULT, null, null);

        ApplicationAnswerRequest ar1 = createAnsReq(1L);
        ApplicationAnswerRequest ar2 = createAnsReq(2L);
        ApplicationAnswerRequest ar3 = createAnsReq(3L);
        ApplicationAnswerRequest ar4 = createAnsReq(4L);
        ApplicationAnswerRequest ar10 = createAnsReq(10L);
        ApplicationAnswerRequest ar11 = createAnsReq(11L);

        CreateApplicationRequest req = CreateApplicationRequest.builder()
                .major("공학")
                .studentId("20201234")
                .grade(3)
                .inSchool(true)
                .isPersonalInfoConsent(true)
                .part(Part.BACKEND)
                .departmentType(null)
                .portfolio(null)
                .answers(List.of(ar10, ar11, ar1, ar2, ar3, ar4))
                .build();

        when(recruitmentRepository.findById(recId)).thenReturn(Optional.of(recruitment));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(questionRepository.findById(1L)).thenReturn(Optional.of(q1));
        when(questionRepository.findById(2L)).thenReturn(Optional.of(q2));
        when(questionRepository.findById(3L)).thenReturn(Optional.of(q3));
        when(questionRepository.findById(4L)).thenReturn(Optional.of(q4));
        when(questionRepository.findById(10L)).thenReturn(Optional.of(dq1));
        when(questionRepository.findById(11L)).thenReturn(Optional.of(dq2));
        when(questionFilter.getRequiredQuestions(recruitment, req.getPart(), req.getDepartmentType()))
                .thenReturn(List.of(dq1, dq2, q1, q2, q3, q4));

        // When
        service.createApplication(recId, userId, true, req);

        // Then
        ArgumentCaptor<Application> captor = ArgumentCaptor.forClass(Application.class);
        verify(applicationRepository).save(captor.capture());
        Application saved = captor.getValue();

        assertAll(
                () -> assertThat(saved.getStatus()).isEqualTo(ApplicationStatus.SUBMITTED),
                () -> assertThat(saved.getAnswers()).hasSize(6)
                        .extracting(Answer::getQuestion)
                        .containsExactlyInAnyOrder(dq1, dq2, q1, q2, q3, q4)
        );
    }

    @Test
    void createApplication_submit_withDefaultQuestions_missingDefaultAnswers_throws() {
        // Given: QuestionFilter가 DEFAULT 질문 포함하여 반환하지만, answers에 DEFAULT 답변 누락
        Question dq1 = createQuestion(10L, QuestionTarget.DEFAULT, null, null);

        ApplicationAnswerRequest ar1 = createAnsReq(1L);
        ApplicationAnswerRequest ar2 = createAnsReq(2L);
        ApplicationAnswerRequest ar3 = createAnsReq(3L);
        ApplicationAnswerRequest ar4 = createAnsReq(4L);

        CreateApplicationRequest req = CreateApplicationRequest.builder()
                .major("공학")
                .studentId("20201234")
                .grade(3)
                .inSchool(true)
                .isPersonalInfoConsent(true)
                .part(Part.BACKEND)
                .departmentType(null)
                .portfolio(null)
                .answers(List.of(ar1, ar2, ar3, ar4))
                .build();

        when(recruitmentRepository.findById(recId)).thenReturn(Optional.of(recruitment));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(questionRepository.findById(1L)).thenReturn(Optional.of(q1));
        when(questionRepository.findById(2L)).thenReturn(Optional.of(q2));
        when(questionRepository.findById(3L)).thenReturn(Optional.of(q3));
        when(questionRepository.findById(4L)).thenReturn(Optional.of(q4));
        when(questionFilter.getRequiredQuestions(recruitment, req.getPart(), req.getDepartmentType()))
                .thenReturn(List.of(dq1, q1, q2, q3, q4));

        // When / Then: DEFAULT 질문 답변이 빠져서 INVALID_ANSWER_SET 발생
        assertThatThrownBy(() -> service.createApplication(recId, userId, true, req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining(ApplicationErrorCode.INVALID_ANSWER_SET.getMessage());
    }

    @Test
    void deleteApplication_success() {
        // given
        int currentGeneration = 13;
        UserInfo userInfo = UserInfo.from(user, currentGeneration);

        Application app = Application.builder().portfolio("https://example.com/portfolio.pdf").build();
        when(applicationRepository.findById(appId)).thenReturn(Optional.of(app));

        // when
        service.deleteApplication(appId, userInfo);

        // then
        verify(applicationRepository).delete(app);
    }

}