package com.snut_likelion.domain.project.dto.req;

import com.snut_likelion.domain.project.entity.ProjectCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
public class CreateProjectPresignedRequest {

    @NotBlank(message = "프로젝트 이름을 입력해주세요.")
    private String name;

    @NotBlank(message = "프로젝트 한 줄 소개를 입력해주세요.")
    private String intro;

    @NotBlank(message = "프로젝트 설명을 입력해주세요.")
    private String description;

    @NotNull(message = "프로젝트 기수를 입력해주세요.")
    @Positive(message = "프로젝트 기수는 1 이상이어야 합니다.")
    private Integer generation;

    @NotNull(message = "프로젝트 카테고리를 선택해주세요.")
    private ProjectCategory category;

    @NotEmpty(message = "프로젝트 이미지는 최소 1장 이상 필요합니다.")
    private List<@NotBlank(message = "storedFileName은 빈 값일 수 없습니다.") String> imageStoredFileNames;

    private String websiteUrl;
    private String playstoreUrl;
    private String appstoreUrl;

    private List<@NotBlank(message = "stack은 빈 값일 수 없습니다.") String> stacks;
}
