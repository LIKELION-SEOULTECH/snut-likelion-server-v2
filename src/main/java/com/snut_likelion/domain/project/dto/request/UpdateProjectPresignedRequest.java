package com.snut_likelion.domain.project.dto.request;

import com.snut_likelion.domain.project.entity.ProjectCategory;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
public class UpdateProjectPresignedRequest {

    private String name;
    private String intro;
    private String description;
    private Integer generation;

    private List<@NotBlank(message = "tag는 빈 값일 수 없습니다.") String> tags;

    private ProjectCategory category;
    private String websiteUrl;
    private String playstoreUrl;
    private String appstoreUrl;

    private List<@NotBlank(message = "storedFileName은 빈 값일 수 없습니다.") String> newImageStoredFileNames;

    @AssertTrue(message = "수정할 값이 없습니다.")
    private boolean isAnyFieldProvided() {
        return (name != null && !name.isBlank())
                || (intro != null && !intro.isBlank())
                || (description != null && !description.isBlank())
                || generation != null
                || (tags != null && !tags.isEmpty())
                || category != null
                || (websiteUrl != null && !websiteUrl.isBlank())
                || (playstoreUrl != null && !playstoreUrl.isBlank())
                || (appstoreUrl != null && !appstoreUrl.isBlank())
                || (newImageStoredFileNames != null && !newImageStoredFileNames.isEmpty());
    }
}
