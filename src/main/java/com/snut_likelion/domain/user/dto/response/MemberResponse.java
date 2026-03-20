package com.snut_likelion.domain.user.dto.response;

import com.snut_likelion.domain.user.entity.LionInfo;
import com.snut_likelion.domain.user.entity.Part;
import com.snut_likelion.domain.user.entity.Role;
import com.snut_likelion.domain.user.entity.User;
import com.snut_likelion.global.common.support.RoleConverter;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberResponse {

    private Long id;
    private String name;
    private String profileImageUrl;
    private Integer generation;
    private String role;
    private String intro;
    private String major;
    private Part part;
    private List<PortfolioLinkDto> portfolioLinks;

    @Builder
    public MemberResponse(Long id, String name, String profileImageUrl, Integer generation, Role role, String major, Part part, String intro, List<PortfolioLinkDto> portfolioLinks) {
        this.id = id;
        this.name = name;
        this.profileImageUrl = profileImageUrl;
        this.generation = generation;
        this.role = RoleConverter.convert(role);
        this.major = major;
        this.part = part;
        this.intro = intro;
        this.portfolioLinks = portfolioLinks;
    }

    public static MemberResponse of(User member, LionInfo lionInfo, List<PortfolioLinkDto> portfolioLinks) {
        return MemberResponse.builder()
                .id(member.getId())
                .name(member.getUsername())
                .profileImageUrl(member.getProfileImageUrl())
                .generation(lionInfo.getGeneration())
                .role(lionInfo.getRole())
                .major(member.getMajor())
                .part(lionInfo.getPart())
                .intro(member.getIntro())
                .portfolioLinks(portfolioLinks)
                .build();
    }
}
