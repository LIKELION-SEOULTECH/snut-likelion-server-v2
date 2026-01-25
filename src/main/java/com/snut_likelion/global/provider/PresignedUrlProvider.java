package com.snut_likelion.global.provider;

import java.time.Duration;
import java.util.Map;

public interface PresignedUrlProvider {

    PresignedPutUrl issuePutUrl(String storedFileName, String contentType, Duration expires);

    record PresignedPutUrl(
            String url,
            Map<String, String> headers
    ) {}
}