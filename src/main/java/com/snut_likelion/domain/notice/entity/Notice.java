package com.snut_likelion.domain.notice.entity;

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
public class Notice extends BaseEntity {

    @Column(nullable = false, length = 100)  // 길이 제한 필요한가??
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private Boolean pinned = false;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @OneToMany(mappedBy = "notice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NoticeAttachment> attachments = new ArrayList<>();

    @Builder
    private Notice(String title, String content, Boolean pinned, String summary) {
        this.title = title;
        this.content = content;
        this.pinned = pinned != null && pinned;
        this.summary = summary;
    }

    public void addAttachment(NoticeAttachment attachment) {
        this.attachments.add(attachment);
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void update(String title, String content, Boolean pinned) {
        this.title = title;
        this.content = content;
        this.pinned = pinned;
    }
}