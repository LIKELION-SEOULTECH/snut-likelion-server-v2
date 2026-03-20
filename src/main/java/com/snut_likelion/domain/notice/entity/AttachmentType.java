package com.snut_likelion.domain.notice.entity;

import com.snut_likelion.domain.file.dto.FileStorageType;
import lombok.Getter;

/**
 * 공지사항 첨부파일 유형
 * - IMAGE : 이미지 첨부 (images/notices/...)
 * - FILE  : 일반 파일 첨부 (files/notices/...)
 */
@Getter
public enum AttachmentType {

    IMAGE(FileStorageType.IMAGE),
    FILE(FileStorageType.FILE);

    private final FileStorageType storageType;

    AttachmentType(FileStorageType storageType) {
        this.storageType = storageType;
    }
}
