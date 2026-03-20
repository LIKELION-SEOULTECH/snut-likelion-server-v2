package com.snut_likelion.domain.project.entity;

import com.snut_likelion.global.common.support.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "project_keywords")
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectTag extends BaseEntity {

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", referencedColumnName = "id")
    private Project project;

    public ProjectTag(String name) {
        this.name = name;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
