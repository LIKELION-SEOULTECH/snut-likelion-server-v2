package com.snut_likelion.domain.recruitment.entity;

import com.snut_likelion.domain.user.entity.Part;
import com.snut_likelion.domain.user.entity.User;
import com.snut_likelion.global.support.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "`applications`")
public class Application extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(length = 100, nullable = false)
    private String major; // 전공

    @Column(nullable = false)
    private Boolean inSchool;

    @Column(nullable = false, unique = true)
    private String studentId;

    @Column(nullable = false)
    private int grade;

    @Column(nullable = false)
    private Boolean isPersonalInfoConsent;

    private String portfolio; // 포트폴리오 URL

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Part part; // 지원 파트

    @Enumerated(EnumType.STRING)
    private DepartmentType departmentType; // 부서 (운영진 지원일 경우)

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ApplicationStatus status = ApplicationStatus.DRAFT; // 지원 상태 (대기, 합격, 불합격 등)

    private LocalDateTime submittedAt; // 지원서 제출 시간

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruitment_id", referencedColumnName = "id")
    private Recruitment recruitment; // 해당 지원서가 속한 모집 공고

    @OneToMany(
            mappedBy = "application",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Answer> answers = new ArrayList<>();

    @Builder
    public Application(Long id, String major, Boolean inSchool, String studentId, int grade, Boolean isPersonalInfoConsent, String portfolio, Part part, DepartmentType departmentType, ApplicationStatus status, LocalDateTime submittedAt) {
        this.id = id;
        this.major = major;
        this.inSchool = inSchool;
        this.studentId = studentId;
        this.grade = grade;
        this.isPersonalInfoConsent = isPersonalInfoConsent;
        this.portfolio = portfolio;
        this.part = part;
        this.departmentType = departmentType;
        this.status = status == null ? ApplicationStatus.DRAFT : status;
        this.submittedAt = submittedAt != null ? submittedAt : LocalDateTime.now();
    }

    public void setPortfolio(String portfolio) {
        this.portfolio = portfolio;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public void submit() {
        this.status = ApplicationStatus.SUBMITTED;
        this.submittedAt = LocalDateTime.now();
    }

    /**
     * === 연관관계 메서드 ===
     */

    public void addAnswer(Answer answer) {
        answers.add(answer);
        answer.setApplication(this);
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setRecruitment(Recruitment recruitment) {
        this.recruitment = recruitment;
    }

    public void update(String major, String studentId, int grade, Boolean inSchool, Boolean isPersonalInfoConsent, Part part, DepartmentType departmentType) {
        this.major = major;
        this.studentId = studentId;
        this.grade = grade;
        this.inSchool = inSchool;
        this.isPersonalInfoConsent = isPersonalInfoConsent;
        this.part = part;
        this.departmentType = departmentType;
    }
}
