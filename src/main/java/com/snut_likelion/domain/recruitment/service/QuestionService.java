package com.snut_likelion.domain.recruitment.service;

import com.snut_likelion.domain.recruitment.dto.res.QuestionResponse;
import com.snut_likelion.domain.recruitment.entity.DepartmentType;
import com.snut_likelion.domain.recruitment.entity.Question;
import com.snut_likelion.domain.recruitment.entity.Recruitment;
import com.snut_likelion.domain.recruitment.exception.RecruitmentErrorCode;
import com.snut_likelion.domain.recruitment.infra.QuestionFilter;
import com.snut_likelion.domain.recruitment.infra.RecruitmentRepository;
import com.snut_likelion.domain.user.entity.Part;
import com.snut_likelion.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final RecruitmentRepository recruitmentRepository;
    private final QuestionFilter questionFilter;

    public List<QuestionResponse> getQuestions(Long recId, Part part, DepartmentType department) {
        Recruitment recruitment = recruitmentRepository.findWithQuestionsById(recId)
                .orElseThrow(() -> new NotFoundException(RecruitmentErrorCode.NOT_FOUND_RECRUITMENT));
        List<Question> questions = questionFilter.getRequiredQuestions(recruitment, part, department);
        return questions.stream()
                .map(QuestionResponse::from)
                .toList();
    }

}
