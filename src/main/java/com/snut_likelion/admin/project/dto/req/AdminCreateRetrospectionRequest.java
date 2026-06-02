package com.snut_likelion.admin.project.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "관리자 프로젝트 회고 등록 요청 (특정 멤버의 회고를 대신 등록)")
public class AdminCreateRetrospectionRequest {

    @Schema(description = "회고를 등록할 회원 ID", example = "42")
    @NotNull(message = "회원 ID를 입력해주세요.")
    private Long memberId;

    @Schema(description = "회고 내용", example = "이번 프로젝트에서 협업과 배포 자동화를 많이 배웠습니다.")
    @NotBlank(message = "내용을 입력해주세요.")
    private String content;
}
