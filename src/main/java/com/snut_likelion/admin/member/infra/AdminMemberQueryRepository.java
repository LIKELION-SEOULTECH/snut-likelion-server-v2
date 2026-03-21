package com.snut_likelion.admin.member.infra;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.snut_likelion.admin.member.dto.res.MemberPageResponse;
import com.snut_likelion.domain.user.entity.Part;
import com.snut_likelion.domain.user.entity.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.snut_likelion.domain.user.entity.QLionInfo.lionInfo;
import static com.snut_likelion.domain.user.entity.QUser.user;

@Repository
@RequiredArgsConstructor
public class AdminMemberQueryRepository {

    private final JPAQueryFactory query;

    public Page<MemberPageResponse.MemberListResponse> getMemberList(
            Integer generation, Part part, Role role, String keyword, Pageable pageable
    ) {
        List<MemberPageResponse.MemberListResponse> result = query
                .select(
                        Projections.constructor(
                                MemberPageResponse.MemberListResponse.class,
                                user.id,
                                user.username,
                                lionInfo.generation,
                                lionInfo.part,
                                lionInfo.role,
                                lionInfo.departmentType.as("department")
                        )
                )
                .from(user)
                .join(lionInfo).on(user.id.eq(lionInfo.user.id))
                .where(
                        matchGeneration(generation),
                        matchPart(part),
                        matchRole(role),
                        searchKeyword(keyword)
                )
                .orderBy(lionInfo.generation.desc(), user.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = query
                .select(user.count())
                .from(user)
                .join(lionInfo).on(user.id.eq(lionInfo.user.id))
                .where(
                        matchGeneration(generation),
                        matchPart(part),
                        matchRole(role),
                        searchKeyword(keyword)
                );

        return PageableExecutionUtils.getPage(result, pageable, countQuery::fetchOne);
    }

    private BooleanExpression searchKeyword(String keyword) {
        return keyword == null ? null : user.username.like("%" + keyword + "%");
    }

    private BooleanExpression matchRole(Role role) {
        return role == null ? null : lionInfo.role.eq(role);
    }

    public BooleanExpression matchGeneration(Integer generation) {
        return generation == null ? null : lionInfo.generation.eq(generation);
    }

    public BooleanExpression matchPart(Part part) {
        return part == null ? null : lionInfo.part.eq(part);
    }
}
