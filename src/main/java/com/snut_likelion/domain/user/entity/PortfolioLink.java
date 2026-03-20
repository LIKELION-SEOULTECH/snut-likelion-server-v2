package com.snut_likelion.domain.user.entity;

import com.snut_likelion.global.common.support.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "portfolio_links")
public class PortfolioLink extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private PortFolioLinkType name;

    @Column(nullable = false, length = 200)
    private String url;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Builder
    public PortfolioLink(PortFolioLinkType name, String url) {
        this.name = name;
        this.url = url;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
