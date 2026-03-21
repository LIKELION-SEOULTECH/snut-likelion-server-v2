package com.snut_likelion.infra.ai.repository;

import com.snut_likelion.ai.exception.AiErrorCode;
import com.snut_likelion.ai.exception.AiException;
import com.snut_likelion.ai.repository.AiSummaryRepository;
import com.snut_likelion.infra.ai.AiServerSummarizeRequest;
import com.snut_likelion.infra.ai.AiServerSummarizeResponse;
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
public class AiSummaryRepositoryImpl implements AiSummaryRepository {

    private final SummaryFeignClient summaryFeignClient;

    @Override
    public String summarize(String text) {
        try {
            AiServerSummarizeResponse response = summaryFeignClient.summarize(new AiServerSummarizeRequest(text));

            if (response == null || response.getSummary() == null) {
                log.error("AI summarize returned empty response.");
                throw new AiException(AiErrorCode.AI_SUMMARIZE_EMPTY_RESPONSE);
            }

            return response.getSummary();
        } catch (RetryableException ex) {
            log.error("AI summarize retryable call failed.", ex);
            throw new AiException(AiErrorCode.AI_SUMMARIZE_CALL_FAILED);
        } catch (DecodeException | EncodeException ex) {
            log.error("AI summarize serialization/deserialization failed.", ex);
            throw new AiException(AiErrorCode.AI_SUMMARIZE_CALL_FAILED);
        } catch (FeignException ex) {
            log.error("AI summarize call failed with status {}.", ex.status(), ex);
            throw new AiException(AiErrorCode.AI_SUMMARIZE_CALL_FAILED);
        }
    }
}
