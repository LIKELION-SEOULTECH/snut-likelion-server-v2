package com.snut_likelion.domain.project.dto.req;

import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreateRetrospectionRequest {

    @NotEmpty(message = "내용을 입력해주세요.")
    private String content;

    public CreateRetrospectionRequest(String content) {
        this.content = content;
    }
}
