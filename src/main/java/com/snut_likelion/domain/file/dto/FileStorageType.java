package com.snut_likelion.domain.file.dto;

import lombok.Getter;

/**
 * S3 저장 루트 경로를 결정한다.
 * - IMAGE : images/{category}/{uuid}-{name}.{ext}
 * - FILE  : files/{category}/{uuid}-{name}.{ext}
 */
@Getter
public enum FileStorageType {

    IMAGE("images"),
    FILE("files");

    private final String storageRoot;

    FileStorageType(String storageRoot) {
        this.storageRoot = storageRoot;
    }
}
