package com.snut_likelion.domain.user.entity;

import com.snut_likelion.domain.project.entity.ProjectParticipation;
import com.snut_likelion.domain.recruitment.entity.DepartmentType;
import com.snut_likelion.global.common.support.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "lion_info",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK_LIONINFO_USER_GENERATION",
                        columnNames = {"user_id", "generation"}
                )
        }
)
public class LionInfo extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Part part; // 파트

    @Column(nullable = false)
    private int generation;

    @Enumerated(EnumType.STRING)
    private DepartmentType departmentType;

    @OneToMany(mappedBy = "lionInfo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectParticipation> participations = new ArrayList<>();

    @Builder
    public LionInfo(Role role, Part part, int generation, DepartmentType departmentType) {
        this.role = role;
        this.part = part;
        this.generation = generation;
        this.departmentType = departmentType;
    }

    public static LionInfo of(int generation, Part part, Role role) {
        return LionInfo.builder()
                .generation(generation)
                .part(part)
                .role(role)
                .build();
    }

    public void updateByAdmin(Part part, Role role, DepartmentType departmentType) {
        this.part = part;
        this.role = role;
        this.departmentType = departmentType;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void addProjectParticipation(ProjectParticipation participation) {
        this.participations.add(participation);
        participation.setLionInfo(this);
    }
}
