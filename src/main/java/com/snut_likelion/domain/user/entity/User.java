package com.snut_likelion.domain.user.entity;


import com.snut_likelion.domain.recruitment.entity.Application;
import com.snut_likelion.domain.recruitment.entity.DepartmentType;
import com.snut_likelion.global.common.support.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User extends BaseEntity {

    @Column(unique = true)
    private String email;

    private String username; // 실제 이름

    @Column(nullable = false)
    private String password;

    @Column(unique = true)
    private String phoneNumber; // 연락처

    @Column(length = 255)
    private String intro; // 한 줄 소개

    @Lob
    private String description; // 자기 소개

    @Column(length = 200)
    private String saying; // 명언

    @Column(length = 100)
    private String major; // 전공

    @Lob
    private String profileImageUrl;

    private String stacks;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LionInfo> lionInfos = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PortfolioLink> portfolioLinks = new ArrayList<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Application application;

    @Builder
    public User(Long id, String email, String username, String password, String phoneNumber, String intro, String description, String saying, String major, String profileImageUrl, String stacks) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.intro = intro;
        this.description = description;
        this.saying = saying;
        this.major = major;
        this.profileImageUrl = profileImageUrl;
        this.stacks = stacks;
    }

    public List<String> getStackList() {
        if (stacks == null || stacks.isEmpty()) {
            return new ArrayList<>();
        }
        return List.of(stacks.split(", "));
    }

    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

    public void changeProfileImage(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void setPortfolioLinkList(List<PortfolioLink> portfolioLinkList) {
        this.portfolioLinks.clear();
        portfolioLinkList.forEach(portfolioLink -> {
            portfolioLink.setUser(this);
            this.portfolioLinks.add(portfolioLink);
        });
    }

    public void updateProfile(String intro, String description, String major, String saying, List<String> stacks) {
        if (StringUtils.hasText(intro)) this.intro = intro;
        if (StringUtils.hasText(description)) this.description = description;
        if (StringUtils.hasText(major)) this.major = major;
        if (StringUtils.hasText(saying)) this.saying = saying;
        if (stacks != null && !stacks.isEmpty()) {
            this.stacks = String.join(", ", stacks);
        }
    }

    public void addLionInfo(LionInfo lionInfo) {
        this.lionInfos.add(lionInfo);
        lionInfo.setUser(this);
    }

    public void generateCurrentLionInfo(int currentGeneration, Part part, Role role, DepartmentType departmentType) {
        for (LionInfo existing : this.lionInfos) {
            if (existing.getGeneration() == currentGeneration) {
                existing.updateByAdmin(part, role, departmentType);
                return;
            }
        }

        LionInfo lionInfo = LionInfo.builder()
                .generation(currentGeneration)
                .part(part)
                .role(role)
                .departmentType(departmentType)
                .build();

        this.addLionInfo(lionInfo);
    }

    public void updateUsername(String username) {
        this.username = username;
    }

    public void setLionInfos(List<LionInfo> newLionInfos) {
        this.lionInfos.clear();
        this.lionInfos.addAll(newLionInfos);
    }
}
