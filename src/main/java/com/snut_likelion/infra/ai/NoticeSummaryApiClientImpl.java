package com.snut_likelion.infra.ai;

import com.snut_likelion.domain.notice.repository.SummaryApiClient;
import com.snut_likelion.infra.ai.client.SummaryFeignClient;
import feign.FeignException;
import feign.RetryableException;
import feign.codec.DecodeException;
import feign.codec.EncodeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NoticeSummaryApiClientImpl implements SummaryApiClient {

    private static final String FALLBACK_SUMMARY = "요약이 제공되지 않았습니다.";

    private final SummaryFeignClient summaryFeignClient;

    @Override
    public String summarize(String content) {
        try {
            AiServerSummarizeResponse response = summaryFeignClient.summarize(new AiServerSummarizeRequest(content));

            if (response == null || response.getSummary() == null) {
                log.warn("공지 요약 API 응답이 비어 있습니다.");
                return FALLBACK_SUMMARY;
            }

            return response.getSummary();
        } catch (RetryableException | DecodeException | EncodeException ex) {
            log.warn("공지 요약 API 네트워크/직렬화 에러", ex);
        } catch (FeignException ex) {
            log.warn("공지 요약 API 에러: status={}", ex.status(), ex);
        }

        return FALLBACK_SUMMARY;
    }
}
