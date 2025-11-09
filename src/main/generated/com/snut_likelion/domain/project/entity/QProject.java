package com.snut_likelion.domain.project.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QProject is a Querydsl query type for Project
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProject extends EntityPathBase<Project> {

    private static final long serialVersionUID = -1666482166L;

    public static final QProject project = new QProject("project");

    public final com.snut_likelion.global.support.QBaseEntity _super = new com.snut_likelion.global.support.QBaseEntity(this);

    public final StringPath appstoreUrl = createString("appstoreUrl");

    public final EnumPath<ProjectCategory> category = createEnum("category", ProjectCategory.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath description = createString("description");

    public final NumberPath<Integer> generation = createNumber("generation", Integer.class);

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final StringPath images = createString("images");

    public final StringPath intro = createString("intro");

    public final StringPath name = createString("name");

    public final ListPath<ProjectParticipation, QProjectParticipation> participations = this.<ProjectParticipation, QProjectParticipation>createList("participations", ProjectParticipation.class, QProjectParticipation.class, PathInits.DIRECT2);

    public final StringPath playstoreUrl = createString("playstoreUrl");

    public final ListPath<ProjectRetrospection, QProjectRetrospection> retrospections = this.<ProjectRetrospection, QProjectRetrospection>createList("retrospections", ProjectRetrospection.class, QProjectRetrospection.class, PathInits.DIRECT2);

    public final StringPath tags = createString("tags");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final StringPath websiteUrl = createString("websiteUrl");

    public QProject(String variable) {
        super(Project.class, forVariable(variable));
    }

    public QProject(Path<? extends Project> path) {
        super(path.getType(), path.getMetadata());
    }

    public QProject(PathMetadata metadata) {
        super(Project.class, metadata);
    }

}

