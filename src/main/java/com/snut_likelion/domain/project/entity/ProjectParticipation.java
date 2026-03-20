package com.snut_likelion.domain.project.entity;

import com.snut_likelion.domain.user.entity.LionInfo;
import com.snut_likelion.global.common.support.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "project_participation")
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectParticipation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "likeLionInfo_id", referencedColumnName = "id")
    private LionInfo lionInfo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", referencedColumnName = "id")
    private Project project;

    public ProjectParticipation(LionInfo lionInfo, Project project) {
        this.lionInfo = lionInfo;
        this.project = project;
    }

    public void setLionInfo(LionInfo lionInfo) {
        this.lionInfo = lionInfo;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
