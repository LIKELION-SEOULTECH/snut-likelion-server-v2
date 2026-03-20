package com.snut_likelion.global.support;

import com.snut_likelion.domain.user.entity.Role;

public class RoleConverter {

    public static String convert(Role role) {
        switch (role) {
            case ROLE_ADMIN -> {
                return "대표";
            }
            case ROLE_MANAGER -> {
                return "운영진";
            }
            case ROLE_USER -> {
                return "아기사자";
            }
            default -> throw new IllegalArgumentException("일치하는 역할이 없습니다: " + role);
        }
    }
}
