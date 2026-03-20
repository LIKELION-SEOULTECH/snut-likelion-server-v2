package com.snut_likelion.domain.project.entity;

import com.snut_likelion.domain.user.entity.User;
import com.snut_likelion.global.common.support.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "project_retrospection")
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectRetrospection extends BaseEntity {

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id", referencedColumnName = "id")
    private User writer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", referencedColumnName = "id")
    private Project project;

    public ProjectRetrospection(String content) {
        this.content = content;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void setWriter(User writer) {
        this.writer = writer;
    }

    public void updateContent(String content) {
        this.content = content;
    }
}
