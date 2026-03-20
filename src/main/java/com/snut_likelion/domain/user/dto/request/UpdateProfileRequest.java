package com.snut_likelion.domain.user.dto.request;

import com.snut_likelion.domain.user.entity.PortFolioLinkType;
import com.snut_likelion.domain.user.entity.PortfolioLink;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "프로필 수정 요청")
public class UpdateProfileRequest {

    @Schema(description = "프로필 이미지 storedFileName (S3 key, upload-complete 후 받은 값)", example = "images/members/uuid-profile.png", nullable = true)
    private String profileImage;

    @Schema(description = "한 줄 소개", example = "안녕하세요!", nullable = true)
    private String intro;

    @Schema(description = "자기소개", example = "저는 백엔드 개발자입니다.", nullable = true)
    private String description;

    @Schema(description = "좌우명", example = "열심히 하자", nullable = true)
    private String saying;

    @Schema(description = "전공", example = "컴퓨터공학과", nullable = true)
    private String major;

    @Schema(description = "기술 스택 목록", example = "[\"Java\", \"Spring\", \"React\"]", nullable = true)
    private List<String> stacks;

    @Schema(description = "포트폴리오 링크 목록", nullable = true)
    private List<PortfolioLinkDto> portfolioLinks = new ArrayList<>();

    @Builder
    public UpdateProfileRequest(String profileImage, String intro, String description, String saying, String major, List<String> stacks, List<PortfolioLinkDto> portfolioLinks) {
        this.profileImage = profileImage;
        this.intro = intro;
        this.description = description;
        this.saying = saying;
        this.major = major;
        this.stacks = stacks;
        this.portfolioLinks = portfolioLinks == null ? new ArrayList<>() : portfolioLinks;
    }

    @Getter
    @NoArgsConstructor
    public static class PortfolioLinkDto {

        private String name;
        private String url;

        @Builder
        public PortfolioLinkDto(String name, String url) {
            this.name = name;
            this.url = url;
        }

        public PortfolioLink toEntity() {
            return PortfolioLink.builder()
                    .name(PortFolioLinkType.valueOf(name))
                    .url(url)
                    .build();
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

}
