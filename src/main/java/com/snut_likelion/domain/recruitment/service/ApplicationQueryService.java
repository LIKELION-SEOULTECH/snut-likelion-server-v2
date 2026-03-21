package com.snut_likelion.domain.recruitment.service;

import com.snut_likelion.domain.recruitment.dto.res.ApplicationDetailsResponse;
import com.snut_likelion.domain.recruitment.entity.Application;
import com.snut_likelion.domain.recruitment.infra.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationQueryService {

    @Value("${snut.likelion.current-generation}")
    private int currentGeneration;

    private final ApplicationRepository applicationRepository;

    @Transactional(readOnly = true)
    public List<ApplicationDetailsResponse> getMyApplication(Long userId) {
        List<Application> applications = applicationRepository.findMyApplication(userId, currentGeneration);
        return applications.stream()
                .map(ApplicationDetailsResponse::from)
                .toList();
    }
}
