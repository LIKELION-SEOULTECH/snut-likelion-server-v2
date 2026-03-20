package com.snut_likelion.infra.file;

import com.snut_likelion.global.provider.PresignedUrlProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

@Component
@Profile("dev")  // Dev용
@RequiredArgsConstructor
public class LocalPresignedUrlProvider implements PresignedUrlProvider {

    @Value("${server.url:http://localhost:9090}")
    private String serverUrl;

    @Override
    public PresignedPutUrl issuePutUrl(String storedFileName, String contentType, Duration expires) {
        // key에 '/'가 포함되므로 URL 인코딩 처리
        String encodedKey = URLEncoder.encode(storedFileName, StandardCharsets.UTF_8);

        // 가짜 업로드 URL
        String url = serverUrl + "/api/v1/files/local/presigned-put?key=" + encodedKey;

        return new PresignedPutUrl(url, Map.of("Content-Type", contentType));
    }
}