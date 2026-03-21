package com.snut_likelion.domain.project.dto.req;

import com.snut_likelion.domain.project.entity.ProjectCategory;
import com.snut_likelion.domain.project.exception.ProjectErrorCode;
import com.snut_likelion.global.error.exception.BadRequestException;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
public class UpdateProjectRequest {

    private String name;
    private String intro;
    private String description;
    private Integer generation;
    private List<String> tags;
    private ProjectCategory category;
    private String websiteUrl;
    private String playstoreUrl;
    private String appstoreUrl;
    private List<MultipartFile> newImages; // 추가할 이미지
    private List<RetrospectionDto> retrospections;

    @Builder
    public UpdateProjectRequest(String name, String intro, String description, Integer generation, String websiteUrl, String playstoreUrl, String appstoreUrl, List<String> tags, ProjectCategory category, List<MultipartFile> newImages, List<RetrospectionDto> retrospections) {
        boolean allNull = name == null
                && intro == null
                && description == null
                && generation == null
                && (tags == null || tags.isEmpty())
                && category == null
                && (newImages == null || newImages.isEmpty())
                && (retrospections == null || retrospections.isEmpty())
                && websiteUrl == null
                && playstoreUrl == null
                && appstoreUrl == null;

        if (allNull) {
            throw new BadRequestException(ProjectErrorCode.UPDATE_BAD_REQUEST);
        }

        this.name = name;
        this.intro = intro;
        this.description = description;
        this.generation = generation;
        this.websiteUrl = websiteUrl;
        this.playstoreUrl = playstoreUrl;
        this.appstoreUrl = appstoreUrl;
        this.tags = tags;
        this.category = category;
        this.newImages = newImages;
        this.retrospections = retrospections;
    }

}
