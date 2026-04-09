package com.snut_likelion.infra.file;

import com.snut_likelion.global.error.exception.BadRequestException;
import com.snut_likelion.global.error.exception.InternalServerException;
import com.snut_likelion.global.provider.FileProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@Profile("prod")
@RequiredArgsConstructor
public class S3FileProvider implements FileProvider {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    private final S3Client s3Client;

    @Override
    public Resource getFile(String key) {
        try {
            ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(
                    GetObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .build()
            );
            return new InputStreamResource(s3Object);
        } catch (NoSuchKeyException ex) {
            throw new BadRequestException(FileErrorCode.FILE_NOT_FOUND);
        } catch (S3Exception ex) {
            throw new InternalServerException(FileErrorCode.S3_SERVICE_ERROR, ex.awsErrorDetails().errorMessage());
        }
    }

    @Override
    public void deleteFile(String key) {
        try {
            s3Client.deleteObject(
                    DeleteObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .build()
            );
        } catch (S3Exception ex) {
            log.info("S3 파일 삭제 실패: {}", key, ex);
        }
    }

    @Override
    public String storeFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String key = Instant.now().toEpochMilli()
                + "-" + UUID.randomUUID()
                + extension;

        try (InputStream input = file.getInputStream()) {
            PutObjectRequest putReq = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putReq,
                    RequestBody.fromInputStream(input, file.getSize())
            );

            return key;
        } catch (IOException ex) {
            log.error("파일 스트림 처리 실패 [{}]", key, ex);
            throw new InternalServerException(FileErrorCode.IO_ERROR, ex.getMessage());
        } catch (S3Exception ex) {
            log.error("S3 파일 업로드 실패 [{}]: {}", key, ex.awsErrorDetails().errorMessage());
            throw new InternalServerException(FileErrorCode.S3_SERVICE_ERROR, "S3 파일 업로드 실패: " + key);
        }
    }

    @Override
    public String extractImageName(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            throw new BadRequestException(FileErrorCode.INVALID_IMAGE_URL);
        }
        String[] parts = imageUrl.split("/");
        if (parts.length == 0) {
            throw new BadRequestException(FileErrorCode.INVALID_IMAGE_URL);
        }
        return parts[parts.length - 1];
    }

    @Override
    public String buildImageUrl(String key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s",
                bucket,
                region,
                key);
    }

    @Override
    public String buildFileDownloadUrl(String key) {
        return buildImageUrl(key);
    }
}
