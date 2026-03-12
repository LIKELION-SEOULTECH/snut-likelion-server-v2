package com.snut_likelion.infra.ai.repository;

import com.snut_likelion.domain.ai.repository.AiChatRepository;
import com.snut_likelion.infra.ai.client.ChatFeignClient;
import com.snut_likelion.infra.ai.dto.request.AiServerChatRequest;
import com.snut_likelion.infra.ai.dto.response.AiServerChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AiChatRepositoryImpl implements AiChatRepository {

    private final ChatFeignClient chatFeignClient;

    @Override
    public ChatQueryResult chat(String text) {
        AiServerChatResponse response = chatFeignClient.chat(new AiServerChatRequest(text));

        if (response == null) {
            return new ChatQueryResult(null, null);
        }

        return new ChatQueryResult(response.getMatchedQuestion(), response.getScore());
    }
}