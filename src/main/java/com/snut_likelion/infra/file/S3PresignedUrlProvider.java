package com.snut_likelion.infra.file;

import com.snut_likelion.global.provider.PresignedUrlProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.Map;

@Component
@Profile("prod")  // Prod용
@RequiredArgsConstructor
public class S3PresignedUrlProvider implements PresignedUrlProvider {

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    private final S3Presigner s3Presigner;

    @Override
    public PresignedPutUrl issuePutUrl(String storedFileName, String contentType, Duration expires) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(storedFileName)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(expires)
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(presignRequest);

        // 안전하게 Content-Type만 내려줌
        Map<String, String> headers = Map.of("Content-Type", contentType);

        return new PresignedPutUrl(presigned.url().toString(), headers);
    }
}