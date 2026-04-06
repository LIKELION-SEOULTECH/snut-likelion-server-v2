package com.snut_likelion.domain.user.dto.res;

import com.snut_likelion.domain.user.entity.PortFolioLinkType;
import com.snut_likelion.domain.user.entity.PortfolioLink;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PortfolioLinkDto {

    private Long id;
    private PortFolioLinkType name;
    private String url;

    @Builder
    public PortfolioLinkDto(Long id, PortFolioLinkType name, String url) {
        this.id = id;
        this.name = name;
        this.url = url;
    }

    public static PortfolioLinkDto from(PortfolioLink portfolioLink) {
        return PortfolioLinkDto.builder()
                .id(portfolioLink.getId())
                .name(portfolioLink.getName())
                .url(portfolioLink.getUrl())
                .build();
    }
}
