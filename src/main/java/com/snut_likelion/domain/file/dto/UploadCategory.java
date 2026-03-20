package com.snut_likelion.domain.file.dto;

import lombok.Getter;

@Getter
public enum UploadCategory {

    BLOG("blogs"),
    PROJECT("projects"),
    NOTICE("notices"),
    MEMBER("members");

    private final String folderName;

    UploadCategory(String folderName) {
        this.folderName = folderName;
    }
}
