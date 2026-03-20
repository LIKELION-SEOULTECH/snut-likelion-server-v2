package com.snut_likelion.admin.recruitment.service;

import com.snut_likelion.admin.recruitment.dto.req.CreateRecruitmentRequest;
import com.snut_likelion.admin.recruitment.dto.req.UpdateRecruitmentRequest;
import com.snut_likelion.domain.recruitment.entity.Recruitment;
import com.snut_likelion.domain.recruitment.exception.RecruitmentErrorCode;
import com.snut_likelion.domain.recruitment.infra.RecruitmentRepository;
import com.snut_likelion.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminRecruitmentService {

    private final RecruitmentRepository recruitmentRepository;

    @Transactional
    public void createRecruitment(CreateRecruitmentRequest req) {
        Recruitment recruitment = req.toEntity();
        recruitmentRepository.save(recruitment);
    }

    @Transactional
    public void updateRecruitment(Long recId, UpdateRecruitmentRequest req) {
        Recruitment recruitment = recruitmentRepository.findById(recId)
                .orElseThrow(() -> new NotFoundException(RecruitmentErrorCode.NOT_FOUND_RECRUITMENT));
        recruitment.update(req.getGeneration(), req.getRecruitmentType(), req.getOpenDate(), req.getCloseDate());
    }

    @Transactional
    public void removeRecruitment(Long recId) {
        Recruitment recruitment = recruitmentRepository.findById(recId)
                .orElseThrow(() -> new NotFoundException(RecruitmentErrorCode.NOT_FOUND_RECRUITMENT));
        recruitmentRepository.delete(recruitment);
    }
}
