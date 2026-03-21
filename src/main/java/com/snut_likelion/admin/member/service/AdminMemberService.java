package com.snut_likelion.admin.member.service;

import com.snut_likelion.admin.member.dto.req.UpdateMemberRequest;
import com.snut_likelion.admin.member.dto.res.MemberPageResponse;
import com.snut_likelion.admin.member.infra.AdminMemberQueryRepository;
import com.snut_likelion.domain.user.entity.LionInfo;
import com.snut_likelion.domain.user.entity.Part;
import com.snut_likelion.domain.user.entity.Role;
import com.snut_likelion.domain.user.entity.User;
import com.snut_likelion.domain.user.exception.UserErrorCode;
import com.snut_likelion.domain.user.repository.UserRepository;
import com.snut_likelion.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminMemberService {

    private final int PAGE_SIZE = 8;

    private final UserRepository userRepository;
    private final AdminMemberQueryRepository queryRepository;

    public MemberPageResponse getMemberList(Integer generation, Part part, Role role, int page, String keyword) {
        PageRequest pageRequest = PageRequest.of(page, PAGE_SIZE);
        Page<MemberPageResponse.MemberListResponse> result =
                queryRepository.getMemberList(generation, part, role, keyword, pageRequest);
        return MemberPageResponse.from(result);
    }

    @Transactional
    public void updateMember(Long memberId, int generation, UpdateMemberRequest req) {
        User user = userRepository.findWithLionUserById(memberId)
                .orElseThrow(() -> new NotFoundException(UserErrorCode.NOT_FOUND));

        LionInfo targetLionInfo = user.getLionInfos().stream()
                .filter(lionInfo -> lionInfo.getGeneration() == generation)
                .findFirst()
                .orElseThrow(() -> new NotFoundException(UserErrorCode.NOT_FOUND_LION_INFO));

        user.updateUsername(req.getUsername());
        targetLionInfo.updateByAdmin(
                req.getPart(),
                req.getRole(),
                req.getDepartment()
        );
    }

    @Transactional
    public void deleteMember(Long memberId, int generation) {
        User member = userRepository.findWithLionUserById(memberId)
                .orElseThrow(() -> new NotFoundException(UserErrorCode.NOT_FOUND));

        // 기존 lionInfos 복사
        List<LionInfo> lionInfos = new ArrayList<>(member.getLionInfos());

        // 삭제 대상이 있는지 검사
        boolean exists = lionInfos.stream()
                .anyMatch(info -> info.getGeneration() == generation);
        if (!exists) {
            throw new NotFoundException(UserErrorCode.NOT_FOUND_LION_INFO);
        }

        // 실제 삭제
        lionInfos.removeIf(info -> info.getGeneration() == generation);
        member.setLionInfos(lionInfos);
    }
}
