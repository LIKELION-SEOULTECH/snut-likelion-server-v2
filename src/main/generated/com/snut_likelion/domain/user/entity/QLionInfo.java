package com.snut_likelion.domain.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QLionInfo is a Querydsl query type for LionInfo
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QLionInfo extends EntityPathBase<LionInfo> {

    private static final long serialVersionUID = 908014329L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QLionInfo lionInfo = new QLionInfo("lionInfo");

    public final com.snut_likelion.global.support.QBaseEntity _super = new com.snut_likelion.global.support.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final EnumPath<com.snut_likelion.domain.recruitment.entity.DepartmentType> departmentType = createEnum("departmentType", com.snut_likelion.domain.recruitment.entity.DepartmentType.class);

    public final NumberPath<Integer> generation = createNumber("generation", Integer.class);

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final EnumPath<Part> part = createEnum("part", Part.class);

    public final ListPath<com.snut_likelion.domain.project.entity.ProjectParticipation, com.snut_likelion.domain.project.entity.QProjectParticipation> participations = this.<com.snut_likelion.domain.project.entity.ProjectParticipation, com.snut_likelion.domain.project.entity.QProjectParticipation>createList("participations", com.snut_likelion.domain.project.entity.ProjectParticipation.class, com.snut_likelion.domain.project.entity.QProjectParticipation.class, PathInits.DIRECT2);

    public final EnumPath<Role> role = createEnum("role", Role.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final QUser user;

    public QLionInfo(String variable) {
        this(LionInfo.class, forVariable(variable), INITS);
    }

    public QLionInfo(Path<? extends LionInfo> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QLionInfo(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QLionInfo(PathMetadata metadata, PathInits inits) {
        this(LionInfo.class, metadata, inits);
    }

    public QLionInfo(Class<? extends LionInfo> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user"), inits.get("user")) : null;
    }

}

