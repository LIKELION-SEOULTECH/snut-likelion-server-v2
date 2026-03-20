package com.snut_likelion.domain.auth.entity;

import com.snut_likelion.global.common.support.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken extends BaseEntity {

    @Column(nullable = false)
    private String payload;

    @Column(nullable = false, length = 50)
    private String email;

    @Builder
    public RefreshToken(String payload, String email) {
        this.payload = payload;
        this.email = email;
    }

    public static RefreshToken of(String payload, String email) {
        return RefreshToken.builder()
                .payload(payload)
                .email(email)
                .build();
    }

    public void updatePayload(String payload) {
        this.payload = payload;
    }
}