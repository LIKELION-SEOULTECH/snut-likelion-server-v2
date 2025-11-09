package com.snut_likelion.admin.member.dto.request;

import com.snut_likelion.domain.recruitment.entity.DepartmentType;
import com.snut_likelion.domain.user.entity.Part;
import com.snut_likelion.domain.user.entity.Role;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UpdateMemberRequest {

    @NotEmpty(message = "유저명을 입력해주세요.")
    private String username;

//    @NotNull(message = "기수를 입력해주세요.")
//    private int generation;

    @NotNull(message = "파트를 입력해주세요.")
    private Part part;

    @NotNull(message = "역할을 입력해주세요.")
    private Role role;

    private DepartmentType department;

    @Builder
    public UpdateMemberRequest(String username, Part part, Role role, DepartmentType department) {
        this.username = username;
        this.part = part;
        this.role = role;
        this.department = department;

    }
}
