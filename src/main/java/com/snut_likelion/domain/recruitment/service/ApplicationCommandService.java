package com.snut_likelion.domain.recruitment.service;

import com.snut_likelion.domain.recruitment.dto.request.ApplicationAnswerRequest;
import com.snut_likelion.domain.recruitment.dto.request.CreateApplicationRequest;
import com.snut_likelion.domain.recruitment.dto.request.UpdateApplicationRequest;
import com.snut_likelion.domain.recruitment.entity.*;
import com.snut_likelion.domain.recruitment.exception.ApplicationErrorCode;
import com.snut_likelion.domain.recruitment.exception.QuestionErrorCode;
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
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationCommandService {

    private final ApplicationRepository applicationRepository;
    private final RecruitmentRepository recruitmentRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final QuestionFilter questionFilter;

    @Transactional
    public void createApplication(Long recId, Long userId, boolean submit, CreateApplicationRequest req) {
        Application application = req.toEntity();

        Recruitment recruitment = recruitmentRepository.findById(recId)
                .orElseThrow(() -> new NotFoundException(RecruitmentErrorCode.NOT_FOUND_RECRUITMENT));
        application.setRecruitment(recruitment);

        this.mappingAnswers(req.getAnswers(), application);
        this.mappingUser(userId, application);

        if (submit) {
            this.validateAnswerSet(
                    recruitment,
                    req.getPart(),
                    req.getDepartmentType(),
                    req.getAnswers()
            );
            application.submit();
        }

        applicationRepository.save(application);
    }

    private void validateAnswerSet(Recruitment recruitment, Part part, DepartmentType departmentType, List<ApplicationAnswerRequest> answerRequests) {
        Set<Long> requiredQids = questionFilter.getRequiredQuestions(recruitment, part, departmentType)
                .stream()
                .map(Question::getId)
                .collect(Collectors.toSet());

        Set<Long> providedQids = answerRequests.stream()
                .map(ApplicationAnswerRequest::getQuestionId)
                .collect(Collectors.toSet());

        if (!providedQids.equals(requiredQids)) {
            throw new BadRequestException(ApplicationErrorCode.INVALID_ANSWER_SET);
        }
    }

    private void mappingUser(Long userId, Application application) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(UserErrorCode.NOT_FOUND));
        application.setUser(user);
    }

    private void mappingAnswers(List<ApplicationAnswerRequest> answers, Application application) {
        if (answers == null || answers.isEmpty()) {
            throw new BadRequestException(ApplicationErrorCode.EMPTY_ANSWERS);
        }
        answers.stream()
                .map(answer -> {
                    Question question = questionRepository.findById(answer.getQuestionId())
                            .orElseThrow(() -> new NotFoundException(QuestionErrorCode.NOT_FOUND_QUESTION));
                    return answer.toEntity(question, application);
                })
                .forEach(application::addAnswer);
    }

    @Transactional
    @PreAuthorize("@authChecker.isMyApplication(#loginUser, #appId)")
    public void updateApplication(Long appId, UserInfo loginUser, boolean submit, UpdateApplicationRequest req) {
        Application application = applicationRepository.findById(appId)
                .orElseThrow(() -> new NotFoundException(ApplicationErrorCode.NOT_FOUND_APPLICATION));

        if (application.getStatus() == ApplicationStatus.SUBMITTED) {
            throw new BadRequestException(ApplicationErrorCode.ALREADY_SUBMITTED);
        }

        // 답변 업데이트
        application.getAnswers().clear(); // 기존 답변 제거
        this.mappingAnswers(req.getAnswers(), application);

        // 기본 필드 덮어쓰기
        application.update(
                req.getMajor(),
                req.getStudentId(),
                req.getGrade(),
                req.getInSchool(),
                req.getIsPersonalInfoConsent(),
                req.getPortfolio(),
                req.getPart(),
                req.getDepartmentType()
        );

        this.mappingUser(loginUser.getId(), application);

        // 제출 상태 업데이트
        if (submit) {
            this.validateAnswerSet(
                    application.getRecruitment(),
                    req.getPart(),
                    req.getDepartmentType(),
                    req.getAnswers()
            );
            application.submit();
        }

        applicationRepository.save(application);
    }

    @Transactional
    @PreAuthorize("@authChecker.isMyApplication(#loginUser, #appId)")
    public void deleteApplication(Long appId, UserInfo loginUser) {
        Application application = applicationRepository.findById(appId)
                .orElseThrow(() -> new NotFoundException(ApplicationErrorCode.NOT_FOUND_APPLICATION));
        applicationRepository.delete(application);
    }

}
