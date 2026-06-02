package com.snut_likelion.domain.project.dto.res;

import com.snut_likelion.domain.project.entity.ProjectRetrospection;
import com.snut_likelion.domain.user.entity.LionInfo;
import com.snut_likelion.domain.user.entity.Part;
import com.snut_likelion.domain.user.entity.User;
import com.snut_likelion.domain.user.exception.UserErrorCode;
import com.snut_likelion.global.error.exception.NotFoundException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "프로젝트 회고 응답")
public class RetrospectionResponse {

    @Schema(description = "회고 ID", example = "10")
    private Long id;

    @Schema(description = "회고 내용", example = "이번 프로젝트에서 협업과 배포 자동화를 많이 배웠습니다.")
    private String content;

    @Schema(description = "작성자 정보")
    private Writer writer;

    @Builder
    public RetrospectionResponse(Long id, String content, Writer writer) {
        this.id = id;
        this.content = content;
        this.writer = writer;
    }

    public static RetrospectionResponse from(ProjectRetrospection projectRetrospection) {
        return RetrospectionResponse.builder()
                .id(projectRetrospection.getId())
                .content(projectRetrospection.getContent())
                .writer(Writer.from(projectRetrospection.getWriter(), projectRetrospection.getProject().getGeneration()))
                .build();
    }

    @Getter
    @Schema(description = "회고 작성자")
    public static class Writer {

        @Schema(description = "작성자 회원 ID", example = "42")
        private Long id;

        @Schema(description = "작성자 이름", example = "홍길동")
        private String name;

        @Schema(description = "작성자 파트", example = "FRONTEND")
        private String part;

        @Builder
        public Writer(Long id, String name, Part part) {
            this.id = id;
            this.name = name;
            this.part = part.name();
        }

        public static Writer from(User writer, int generation) {
            LionInfo currentLionInfo = writer.getLionInfos().stream()
                    .filter(lionInfo -> lionInfo.getGeneration() == generation)
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException(UserErrorCode.NOT_FOUND_LION_INFO));

            return Writer.builder()
                    .id(writer.getId())
                    .name(writer.getUsername())
                    .part(currentLionInfo.getPart())
                    .build();
        }
    }
}
