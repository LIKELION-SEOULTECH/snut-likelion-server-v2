package com.snut_likelion.admin.blog.infra;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.snut_likelion.admin.blog.dto.res.BlogPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.snut_likelion.domain.blog.entity.QBlogPost.blogPost;

@Repository
@RequiredArgsConstructor
public class AdminBlogQueryRepository {

    private final JPAQueryFactory query;

    public Page<BlogPageResponse.BlogListResponse> getBlogList(
            String category, String keyword, Pageable pageable
    ) {
        List<BlogPageResponse.BlogListResponse> result = query
                .select(
                        Projections.constructor(
                                BlogPageResponse.BlogListResponse.class,
                                blogPost.id,
                                blogPost.title,
                                blogPost.category,
                                blogPost.author.username,
                                blogPost.createdAt
                        )
                )
                .from(blogPost)
                .where(matchCategory(category),
                        searchKeyword(keyword))
                .orderBy(blogPost.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> count = query
                .select(blogPost.count())
                .from(blogPost)
                .where(matchCategory(category),
                        searchKeyword(keyword));

        return PageableExecutionUtils.getPage(result, pageable, count::fetchOne);
    }

    private BooleanExpression matchCategory(String category) {
        return (category == null || category.isBlank())
                ? null
                : blogPost.category.stringValue().equalsIgnoreCase(category);
    }

    private BooleanExpression searchKeyword(String keyword) {
        return (keyword == null || keyword.isBlank())
                ? null
                : blogPost.title.containsIgnoreCase(keyword);
    }
}
