package com.snut_likelion.domain.project.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "프로젝트 회고 작성 요청")
public class CreateRetrospectionRequest {

    @Schema(description = "회고 작성 대상 회원 ID (일반 사용자는 본인만, 매니저는 타 멤버 지정 가능)", example = "42")
    @NotNull(message = "회원 ID를 입력해주세요.")
    private Long memberId;

    @Schema(description = "회고 내용", example = "이번 프로젝트에서 협업과 배포 자동화를 많이 배웠습니다.")
    @NotEmpty(message = "내용을 입력해주세요.")
    private String content;

    public CreateRetrospectionRequest(String content) {
        this.content = content;
    }
}
