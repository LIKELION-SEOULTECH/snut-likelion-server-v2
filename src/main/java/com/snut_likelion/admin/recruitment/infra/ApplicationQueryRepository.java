package com.snut_likelion.admin.recruitment.infra;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.snut_likelion.admin.recruitment.dto.req.ApplicationListStatus;
import com.snut_likelion.admin.recruitment.dto.res.ApplicationPageResponse;
import com.snut_likelion.domain.recruitment.entity.ApplicationStatus;
import com.snut_likelion.domain.user.entity.Part;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.snut_likelion.domain.recruitment.entity.QApplication.application;

@Repository
@RequiredArgsConstructor
public class ApplicationQueryRepository {

    private final JPAQueryFactory query;

    public Page<ApplicationPageResponse.ApplicationListResponse> getApplicationList(
            Long recId, Part part, ApplicationListStatus status, Pageable pageable
    ) {
        List<ApplicationPageResponse.ApplicationListResponse> result = query
                .select(
                        Projections.constructor(
                                ApplicationPageResponse.ApplicationListResponse.class,
                                application.id,
                                application.user.username,
                                application.user.email,
                                application.part,
                                application.departmentType,
                                application.status,
                                application.submittedAt
                        )
                )
                .from(application)
                .where(
                        application.recruitment.id.eq(recId),
                        isPartMatch(part),
                        isStatusMatch(status)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(application.submittedAt.desc())
                .fetch();

        JPAQuery<Long> countQuery = query
                .select(application.count())
                .from(application)
                .where(
                        application.recruitment.id.eq(recId),
                        isPartMatch(part),
                        isStatusMatch(status)
                );

        return PageableExecutionUtils.getPage(result, pageable, countQuery::fetchOne);

    }

    public BooleanExpression isPartMatch(Part part) {
        return part != null ? application.part.eq(part) : null;
    }

    public BooleanExpression isStatusMatch(ApplicationListStatus status) {
        if (status == null) return null;
        ApplicationStatus applicationStatus = ApplicationStatus.valueOf(status.toString());
        return application.status.eq(applicationStatus);
    }

    public long countByRecruitmentIdAndStatus(Long recId, ApplicationStatus status) {
        Long count = query
                .select(application.count())
                .from(application)
                .where(
                        application.recruitment.id.eq(recId),
                        application.status.eq(status)
                )
                .fetchOne();
        return count != null ? count : 0L;
    }

}
