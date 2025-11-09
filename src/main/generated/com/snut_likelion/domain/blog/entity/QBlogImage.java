package com.snut_likelion.domain.blog.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBlogImage is a Querydsl query type for BlogImage
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBlogImage extends EntityPathBase<BlogImage> {

    private static final long serialVersionUID = 1080552883L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBlogImage blogImage = new QBlogImage("blogImage");

    public final com.snut_likelion.global.support.QBaseEntity _super = new com.snut_likelion.global.support.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final QBlogPost post;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final StringPath url = createString("url");

    public QBlogImage(String variable) {
        this(BlogImage.class, forVariable(variable), INITS);
    }

    public QBlogImage(Path<? extends BlogImage> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBlogImage(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBlogImage(PathMetadata metadata, PathInits inits) {
        this(BlogImage.class, metadata, inits);
    }

    public QBlogImage(Class<? extends BlogImage> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.post = inits.isInitialized("post") ? new QBlogPost(forProperty("post"), inits.get("post")) : null;
    }

}

