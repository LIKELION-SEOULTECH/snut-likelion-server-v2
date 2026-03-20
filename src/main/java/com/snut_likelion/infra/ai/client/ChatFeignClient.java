package com.snut_likelion.infra.ai.client;

import com.snut_likelion.infra.ai.dto.request.AiServerChatRequest;
import com.snut_likelion.infra.ai.dto.response.AiServerChatResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "chatFeignClient", url = "${ai-server.url}")
public interface ChatFeignClient {

    @PostMapping("/chat")
    AiServerChatResponse chat(@RequestBody AiServerChatRequest request);
}
