package com.snut_likelion.domain.project.dto.res;

import com.snut_likelion.domain.project.entity.ProjectRetrospection;
import com.snut_likelion.domain.user.entity.LionInfo;
import com.snut_likelion.domain.user.entity.Part;
import com.snut_likelion.domain.user.entity.User;
import com.snut_likelion.domain.user.exception.UserErrorCode;
import com.snut_likelion.global.error.exception.NotFoundException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RetrospectionResponse {

    private Long id;
    private String content;
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
    public static class Writer {
        private Long id;
        private String name;
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
