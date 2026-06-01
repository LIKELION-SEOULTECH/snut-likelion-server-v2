package com.snut_likelion.domain.project.entity;

import com.snut_likelion.global.common.support.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Table(name = "projects")
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Project extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String intro;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private int generation;

    private String websiteUrl;
    private String playstoreUrl;
    private String appstoreUrl;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ProjectCategory category;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectParticipation> participations = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectRetrospection> retrospections = new ArrayList<>();

    private String tags;

    private String stacks;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String images;

    @Builder
    public Project(Long id, String name, String intro, String description, String websiteUrl, String playstoreUrl, String appstoreUrl, int generation, ProjectCategory category) {
        this.id = id;
        this.name = name;
        this.intro = intro;
        this.description = description;
        this.websiteUrl = websiteUrl;
        this.playstoreUrl = playstoreUrl;
        this.appstoreUrl = appstoreUrl;
        this.generation = generation;
        this.category = category;
    }

    public void update(String name, String intro, String description, Integer generation, ProjectCategory category,
                       String websiteUrl, String playstoreUrl, String appstoreUrl, List<String> stacks) {
        if (StringUtils.hasText(name)) this.name = name;
        if (StringUtils.hasText(intro)) this.intro = intro;
        if (StringUtils.hasText(description)) this.description = description;
        if (generation != null) this.generation = generation;
        if (category != null) this.category = category;
        if (StringUtils.hasText(websiteUrl)) this.websiteUrl = websiteUrl;
        if (StringUtils.hasText(playstoreUrl)) this.playstoreUrl = playstoreUrl;
        if (StringUtils.hasText(appstoreUrl)) this.appstoreUrl = appstoreUrl;
        if (stacks != null && !stacks.isEmpty()) this.setStacks(stacks);
    }

    public List<String> getStackList() {
        if (stacks == null || stacks.isEmpty()) {
            return List.of();
        }
        return List.of(stacks.split(", "));
    }

    public void setStacks(List<String> stacks) {
        if (stacks == null || stacks.isEmpty()) {
            this.stacks = null;
            return;
        }
        this.stacks = String.join(", ", stacks).toUpperCase();
    }

    public String getThumbnailUrl() {
        if (images == null || images.isEmpty()) {
            return null;
        }
        String[] imageUrls = images.split(", ");
        return imageUrls.length > 0 ? imageUrls[0] : null;
    }

    public List<String> getTagList() {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }
        return List.of(tags.split(", "));
    }

    public List<String> getImageUrlList() {
        if (images == null || images.isEmpty()) {
            return List.of();
        }
        return List.of(images.split(", "));
    }

    public void addParticipation(ProjectParticipation participation) {
        this.participations.add(participation);
        participation.setProject(this);
    }

    public void addRetrospection(ProjectRetrospection retrospection) {
        this.retrospections.add(retrospection);
        retrospection.setProject(this);
    }

    public void setTags(List<String> projectTags) {
        if (projectTags == null || projectTags.isEmpty()) {
            this.tags = null;
            return;
        }
        this.tags = String.join(", ", projectTags).toUpperCase();
    }

    public void setImages(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            this.images = null;
            return;
        }
        this.images = String.join(", ", imageUrls);
    }

    public void addImage(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }
        if (this.images == null || this.images.isEmpty()) {
            this.images = String.join(", ", imageUrls);
        } else {
            this.images += ", " + String.join(", ", imageUrls);
        }
    }
}
