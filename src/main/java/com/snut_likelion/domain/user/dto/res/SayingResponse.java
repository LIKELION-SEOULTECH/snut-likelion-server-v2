package com.snut_likelion.domain.user.dto.res;

import com.snut_likelion.domain.user.entity.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SayingResponse {

    private String username;
    private String saying;

    @Builder
    public SayingResponse(String username, String saying) {
        this.username = username;
        this.saying = saying;
    }

    public static SayingResponse from(User user) {
        return SayingResponse.builder()
                .username(user.getUsername())
                .saying(user.getSaying())
                .build();
    }
}
