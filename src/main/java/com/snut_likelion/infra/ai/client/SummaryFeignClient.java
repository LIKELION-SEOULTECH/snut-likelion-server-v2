package com.snut_likelion.infra.ai.client;

import com.snut_likelion.infra.ai.dto.request.AiServerSummarizeRequest;
import com.snut_likelion.infra.ai.dto.response.AiServerSummarizeResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "summaryFeignClient", url = "${ai-server.url}")
public interface SummaryFeignClient {

    @PostMapping("/summarize")
    AiServerSummarizeResponse summarize(@RequestBody AiServerSummarizeRequest request);
}
