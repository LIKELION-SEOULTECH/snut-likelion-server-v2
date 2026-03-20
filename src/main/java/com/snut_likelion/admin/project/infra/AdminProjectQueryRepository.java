package com.snut_likelion.admin.project.infra;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.snut_likelion.admin.project.dto.res.ProjectPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.snut_likelion.domain.project.entity.QProject.project;

@Repository
@RequiredArgsConstructor
public class AdminProjectQueryRepository {

    private final JPAQueryFactory query;

    public Page<ProjectPageResponse.ProjectListResponse> getProjectList(
            Integer generation, String keyword, Pageable pageable
    ) {
        List<ProjectPageResponse.ProjectListResponse> result = query
                .select(
                        Projections.constructor(
                                ProjectPageResponse.ProjectListResponse.class,
                                project.id,
                                project.name,
                                project.generation,
                                project.category,
                                project.createdAt
                        )
                )
                .from(project)
                .where(
                        matchGeneration(generation),
                        searchKeyword(keyword)
                )
                .orderBy(project.generation.desc(), project.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = query
                .select(project.count())
                .from(project)
                .where(
                        matchGeneration(generation),
                        searchKeyword(keyword)
                );

        return PageableExecutionUtils.getPage(result, pageable, countQuery::fetchOne);
    }

    private BooleanExpression searchKeyword(String keyword) {
        return keyword == null ? null : project.name.like("%" + keyword + "%");
    }

    public BooleanExpression matchGeneration(Integer generation) {
        return generation == null ? null : project.generation.eq(generation);
    }
}
