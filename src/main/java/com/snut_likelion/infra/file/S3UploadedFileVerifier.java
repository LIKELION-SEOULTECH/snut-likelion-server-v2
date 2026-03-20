package com.snut_likelion.infra.file;

import com.snut_likelion.global.error.exception.InternalServerException;
import com.snut_likelion.global.provider.UploadedFileVerifier;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Component
@Profile("prod")  // Prod 구현체 (S3 HEAD)
@RequiredArgsConstructor
public class S3UploadedFileVerifier implements UploadedFileVerifier {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final S3Client s3Client;

    @Override
    public boolean exists(String storedFileName) {
        try {
            s3Client.headObject(
                    HeadObjectRequest.builder()
                            .bucket(bucket)
                            .key(storedFileName)
                            .build()
            );
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            // 404류는 false, 그 외는 장애로 판단
            if (e.statusCode() == 404) return false;
            throw new InternalServerException(FileErrorCode.S3_SERVICE_ERROR, e.awsErrorDetails().errorMessage());
        }
    }
}
