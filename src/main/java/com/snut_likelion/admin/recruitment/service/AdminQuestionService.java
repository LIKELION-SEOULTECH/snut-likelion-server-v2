package com.snut_likelion.admin.recruitment.service;

import com.snut_likelion.admin.recruitment.dto.req.UpdateQuestionsRequest;
import com.snut_likelion.domain.recruitment.dto.res.QuestionResponse;
import com.snut_likelion.domain.recruitment.entity.Question;
import com.snut_likelion.domain.recruitment.entity.QuestionTarget;
import com.snut_likelion.domain.recruitment.entity.Recruitment;
import com.snut_likelion.domain.recruitment.entity.RecruitmentType;
import com.snut_likelion.domain.recruitment.exception.QuestionErrorCode;
import com.snut_likelion.domain.recruitment.exception.RecruitmentErrorCode;
import com.snut_likelion.domain.recruitment.infra.RecruitmentRepository;
import com.snut_likelion.global.error.exception.BadRequestException;
import com.snut_likelion.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminQuestionService {

    private final RecruitmentRepository recruitmentRepository;

    @Transactional(readOnly = true)
    public List<QuestionResponse> getAllQuestions(Long recId) {
        Recruitment recruitment = recruitmentRepository.findWithQuestionsById(recId)
                .orElseThrow(() -> new NotFoundException(RecruitmentErrorCode.NOT_FOUND_RECRUITMENT));

        return recruitment.getQuestions().stream()
                .sorted(Comparator.comparingInt(Question::getOrderNum))
                .map(QuestionResponse::from)
                .toList();
    }

    @Transactional
    public void updateQuestions(Long recId, List<UpdateQuestionsRequest> req) {
        Recruitment recruitment = recruitmentRepository.findById(recId)
                .orElseThrow(() -> new NotFoundException(RecruitmentErrorCode.NOT_FOUND_RECRUITMENT));

        List<Question> questions = req.stream()
                .map(UpdateQuestionsRequest::toEntity)
                .toList();

        this.validateQuestions(recruitment, req);

        recruitment.setQuestions(questions);
        recruitmentRepository.save(recruitment);
    }

    private void validateQuestions(Recruitment recruitment, List<UpdateQuestionsRequest> req) {
        if (recruitment.getRecruitmentType() == RecruitmentType.MEMBER
                && req.stream()
                .anyMatch(q -> q.getQuestionTarget() == QuestionTarget.DEPARTMENT)) {
            throw new BadRequestException(QuestionErrorCode.INVALID_QUESTION_INCLUDE);
        }
    }
}
