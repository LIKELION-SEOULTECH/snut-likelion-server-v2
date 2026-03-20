package com.snut_likelion.domain.project.dto.req;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RetrospectionDto {
    private Long memberId;
    private String content;

    @Builder
    public RetrospectionDto(Long memberId, String content) {
        this.memberId = memberId;
        this.content = content;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
