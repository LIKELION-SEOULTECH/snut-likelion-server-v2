package com.snut_likelion.domain.project.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QProjectParticipation is a Querydsl query type for ProjectParticipation
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProjectParticipation extends EntityPathBase<ProjectParticipation> {

    private static final long serialVersionUID = 776936791L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QProjectParticipation projectParticipation = new QProjectParticipation("projectParticipation");

    public final com.snut_likelion.global.support.QBaseEntity _super = new com.snut_likelion.global.support.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final com.snut_likelion.domain.user.entity.QLionInfo lionInfo;

    public final QProject project;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QProjectParticipation(String variable) {
        this(ProjectParticipation.class, forVariable(variable), INITS);
    }

    public QProjectParticipation(Path<? extends ProjectParticipation> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QProjectParticipation(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QProjectParticipation(PathMetadata metadata, PathInits inits) {
        this(ProjectParticipation.class, metadata, inits);
    }

    public QProjectParticipation(Class<? extends ProjectParticipation> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.lionInfo = inits.isInitialized("lionInfo") ? new com.snut_likelion.domain.user.entity.QLionInfo(forProperty("lionInfo"), inits.get("lionInfo")) : null;
        this.project = inits.isInitialized("project") ? new QProject(forProperty("project")) : null;
    }

}

