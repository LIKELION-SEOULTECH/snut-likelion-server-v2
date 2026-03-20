package com.snut_likelion.domain.project.dto.req;

import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UpdateRetrospectionRequest {

    @NotEmpty(message = "내용을 입력해주세요.")
    private String content;

    public UpdateRetrospectionRequest(String content) {
        this.content = content;
    }
}
