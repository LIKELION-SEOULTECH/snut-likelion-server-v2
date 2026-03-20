package com.snut_likelion.admin.member.dto.res;

import com.snut_likelion.domain.recruitment.entity.DepartmentType;
import com.snut_likelion.domain.user.entity.Part;
import com.snut_likelion.domain.user.entity.Role;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberPageResponse {

    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private List<MemberListResponse> content;

    @Builder
    public MemberPageResponse(int page, int size, long totalElements, int totalPages, List<MemberListResponse> content) {
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.content = content;
    }

    public static MemberPageResponse from(Page<MemberPageResponse.MemberListResponse> page) {
        return MemberPageResponse.builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    @Getter
    @NoArgsConstructor
    public static class MemberListResponse {
        private Long id;
        private String username;
        private int generation;
        private String part;
        private String role;
        private String department;

        @Builder
        public MemberListResponse(Long id, String username, int generation, Part part, Role role, DepartmentType department) {
            this.id = id;
            this.username = username;
            this.generation = generation;
            this.part = part.getDescription();
            this.role = role.getDescription();
            this.department = department != null ? department.getDescription() : null;
        }
    }
}
