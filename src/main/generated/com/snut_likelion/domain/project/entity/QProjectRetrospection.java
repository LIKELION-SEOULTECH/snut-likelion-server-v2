package com.snut_likelion.domain.project.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QProjectRetrospection is a Querydsl query type for ProjectRetrospection
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProjectRetrospection extends EntityPathBase<ProjectRetrospection> {

    private static final long serialVersionUID = -1029559805L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QProjectRetrospection projectRetrospection = new QProjectRetrospection("projectRetrospection");

    public final com.snut_likelion.global.support.QBaseEntity _super = new com.snut_likelion.global.support.QBaseEntity(this);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final QProject project;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final com.snut_likelion.domain.user.entity.QUser writer;

    public QProjectRetrospection(String variable) {
        this(ProjectRetrospection.class, forVariable(variable), INITS);
    }

    public QProjectRetrospection(Path<? extends ProjectRetrospection> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QProjectRetrospection(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QProjectRetrospection(PathMetadata metadata, PathInits inits) {
        this(ProjectRetrospection.class, metadata, inits);
    }

    public QProjectRetrospection(Class<? extends ProjectRetrospection> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.project = inits.isInitialized("project") ? new QProject(forProperty("project")) : null;
        this.writer = inits.isInitialized("writer") ? new com.snut_likelion.domain.user.entity.QUser(forProperty("writer"), inits.get("writer")) : null;
    }

}

