package com.snut_likelion.domain.recruitment.service;

import com.snut_likelion.admin.recruitment.dto.req.CreateRecruitmentRequest;
import com.snut_likelion.admin.recruitment.dto.req.UpdateRecruitmentRequest;
import com.snut_likelion.domain.recruitment.dto.res.RecruitmentResponse;
import com.snut_likelion.domain.recruitment.entity.Recruitment;
import com.snut_likelion.domain.recruitment.entity.RecruitmentType;
import com.snut_likelion.domain.recruitment.exception.RecruitmentErrorCode;
import com.snut_likelion.domain.recruitment.infra.RecruitmentRepository;
import com.snut_likelion.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecruitmentService {

    private final RecruitmentRepository recruitmentRepository;

    public RecruitmentResponse getRecentRecruitmentInfo(RecruitmentType recruitmentType) {
        Recruitment recruitment = recruitmentRepository.findRecentRecruitment(recruitmentType)
                .orElseThrow(() -> new NotFoundException(RecruitmentErrorCode.NOT_FOUND_RECRUITMENT));
        return RecruitmentResponse.from(recruitment);
    }

}
