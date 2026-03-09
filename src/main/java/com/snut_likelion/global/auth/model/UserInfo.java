package com.snut_likelion.global.auth.model;

import com.snut_likelion.domain.user.entity.LionInfo;
import com.snut_likelion.domain.user.entity.Role;
import com.snut_likelion.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserInfo {
    private Long id;
    private String username;
    private String password;
    private String email;
    private String role;

    @Builder
    public UserInfo(Long id, String username, String password, String email, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
    }

    public static UserInfo from(User user, int currentGeneration) {
        Role role = null;

        if (user.getLionInfos() != null) {
            boolean isAdmin = user.getLionInfos().stream()
                    .anyMatch(lionInfo -> lionInfo.getRole() == Role.ROLE_ADMIN);

            if (isAdmin) {
                role = Role.ROLE_ADMIN;
            } else {
                role = user.getLionInfos().stream()
                        .filter(lionInfo -> lionInfo.getGeneration() == currentGeneration)
                        .findFirst()
                        .map(LionInfo::getRole)
                        .orElse(null);
            }
        }

        return UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .password(user.getPassword())
                .email(user.getEmail())
                .role(role != null ? role.name() : null)
                .build();
    }
}