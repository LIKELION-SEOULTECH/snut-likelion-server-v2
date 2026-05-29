package com.snut_likelion.admin.project.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminCreateRetrospectionRequest {

    @NotNull(message = "회원 ID를 입력해주세요.")
    private Long memberId;

    @NotBlank(message = "내용을 입력해주세요.")
    private String content;
}
