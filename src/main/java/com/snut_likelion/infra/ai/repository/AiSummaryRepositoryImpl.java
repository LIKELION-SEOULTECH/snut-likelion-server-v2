package com.snut_likelion.infra.ai.repository;

import com.snut_likelion.domain.ai.repository.AiSummaryRepository;
import com.snut_likelion.infra.ai.client.SummaryFeignClient;
import com.snut_likelion.infra.ai.dto.request.AiServerSummarizeRequest;
import com.snut_likelion.infra.ai.dto.response.AiServerSummarizeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AiSummaryRepositoryImpl implements AiSummaryRepository {

    private final SummaryFeignClient summaryFeignClient;

    @Override
    public String summarize(String text) {
        AiServerSummarizeResponse response = summaryFeignClient.summarize(new AiServerSummarizeRequest(text));
        return response != null ? response.getSummary() : null;
    }
}