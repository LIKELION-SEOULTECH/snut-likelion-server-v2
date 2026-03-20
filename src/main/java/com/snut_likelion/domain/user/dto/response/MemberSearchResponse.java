package com.snut_likelion.domain.user.dto.response;

import com.snut_likelion.domain.user.entity.Part;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberSearchResponse {
    private Long id;
    private String name;
    private String part;
    private int generation;
    private String profileImageUrl;

    @Builder
    public MemberSearchResponse(Long id, String name, Part part, int generation, String profileImageUrl) {
        this.id = id;
        this.name = name;
        this.part = part.name();
        this.generation = generation;
        this.profileImageUrl = profileImageUrl;
    }

    public MemberSearchResponse withProfileImageUrl(String url) {
        this.profileImageUrl = url;
        return this;
    }
}
