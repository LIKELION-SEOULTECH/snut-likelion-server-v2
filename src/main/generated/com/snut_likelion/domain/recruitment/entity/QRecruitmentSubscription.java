package com.snut_likelion.domain.recruitment.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QRecruitmentSubscription is a Querydsl query type for RecruitmentSubscription
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRecruitmentSubscription extends EntityPathBase<RecruitmentSubscription> {

    private static final long serialVersionUID = 992109927L;

    public static final QRecruitmentSubscription recruitmentSubscription = new QRecruitmentSubscription("recruitmentSubscription");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath email = createString("email");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final EnumPath<SubscriptionType> subscriptionType = createEnum("subscriptionType", SubscriptionType.class);

    public QRecruitmentSubscription(String variable) {
        super(RecruitmentSubscription.class, forVariable(variable));
    }

    public QRecruitmentSubscription(Path<? extends RecruitmentSubscription> path) {
        super(path.getType(), path.getMetadata());
    }

    public QRecruitmentSubscription(PathMetadata metadata) {
        super(RecruitmentSubscription.class, metadata);
    }

}

