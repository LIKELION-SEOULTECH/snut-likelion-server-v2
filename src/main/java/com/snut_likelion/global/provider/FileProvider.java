package com.snut_likelion.global.provider;

import org.springframework.core.io.Resource;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

public interface FileProvider {

    default void setTransactionSynchronizationForImage(String storedFileName) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == TransactionSynchronization.STATUS_ROLLED_BACK) {
                    deleteFile(storedFileName);
                }
            }
        });
    }

    Resource getFile(String storedFileName);

    void deleteFile(String storedFileName);

    // MultipartFile 직접 업로드: 레거시 코드
    String storeFile(MultipartFile file);

    String extractImageName(String imageUrl);

    String buildImageUrl(String storedFileName);

    String buildFileDownloadUrl(String storedFileName);
}
