package com.snut_likelion.infra.ai.repository;

import com.snut_likelion.ai.repository.AiChatRepository;
import com.snut_likelion.infra.ai.AiServerChatRequest;
import com.snut_likelion.infra.ai.AiServerChatResponse;
import com.snut_likelion.infra.ai.client.ChatFeignClient;
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
public class AiChatRepositoryImpl implements AiChatRepository {

    private final ChatFeignClient chatFeignClient;

    @Override
    public ChatQueryResult chat(String text) {
        try {
            AiServerChatResponse response = chatFeignClient.chat(new AiServerChatRequest(text));

            if (response == null) {
                log.warn("AI chat returned empty response. fallback answer will be used.");
                return new ChatQueryResult(null, null);
            }

            return new ChatQueryResult(response.getMatchedQuestion(), response.getScore());
        } catch (RetryableException ex) {
            log.warn("AI chat retryable call failed. fallback answer will be used.", ex);
            return new ChatQueryResult(null, null);
        } catch (DecodeException | EncodeException ex) {
            log.warn("AI chat serialization/deserialization failed. fallback answer will be used.", ex);
            return new ChatQueryResult(null, null);
        } catch (FeignException ex) {
            log.warn("AI chat call failed with status {}. fallback answer will be used.", ex.status(), ex);
            return new ChatQueryResult(null, null);
        }
    }
}
