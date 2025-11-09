package com.snut_likelion.domain.project.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QProjectTag is a Querydsl query type for ProjectTag
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProjectTag extends EntityPathBase<ProjectTag> {

    private static final long serialVersionUID = -643149008L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QProjectTag projectTag = new QProjectTag("projectTag");

    public final com.snut_likelion.global.support.QBaseEntity _super = new com.snut_likelion.global.support.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final StringPath name = createString("name");

    public final QProject project;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QProjectTag(String variable) {
        this(ProjectTag.class, forVariable(variable), INITS);
    }

    public QProjectTag(Path<? extends ProjectTag> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QProjectTag(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QProjectTag(PathMetadata metadata, PathInits inits) {
        this(ProjectTag.class, metadata, inits);
    }

    public QProjectTag(Class<? extends ProjectTag> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.project = inits.isInitialized("project") ? new QProject(forProperty("project")) : null;
    }

}

