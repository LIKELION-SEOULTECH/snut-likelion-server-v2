package com.snut_likelion.admin.notice.infra;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.snut_likelion.admin.notice.dto.res.NoticePageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.snut_likelion.domain.notice.entity.QNotice.notice;

@Repository
@RequiredArgsConstructor
public class AdminNoticeQueryRepository {

    private final JPAQueryFactory query;

    public Page<NoticePageResponse.NoticeListResponse> getNoticeList(
            String keyword, Pageable pageable
    ) {
        List<NoticePageResponse.NoticeListResponse> result = query
                .select(
                        Projections.constructor(
                                NoticePageResponse.NoticeListResponse.class,
                                notice.id,
                                notice.title,
                                notice.pinned,
                                notice.updatedAt
                        )
                )
                .from(notice)
                .where(searchKeyword(keyword))
                .orderBy(
                        notice.pinned.desc(),     // 고정 공지 먼저
                        notice.updatedAt.desc()   // 최신순
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> count = query
                .select(notice.count())
                .from(notice)
                .where(searchKeyword(keyword));

        return PageableExecutionUtils.getPage(result, pageable, count::fetchOne);
    }

    /* ----- 조건 ----- */
    private BooleanExpression searchKeyword(String keyword) {
        return (keyword == null || keyword.isBlank())
                ? null
                : notice.title.containsIgnoreCase(keyword);
    }
}
