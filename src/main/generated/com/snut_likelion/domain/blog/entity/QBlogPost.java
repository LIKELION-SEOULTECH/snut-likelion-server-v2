package com.snut_likelion.domain.blog.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBlogPost is a Querydsl query type for BlogPost
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBlogPost extends EntityPathBase<BlogPost> {

    private static final long serialVersionUID = 727804232L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBlogPost blogPost = new QBlogPost("blogPost");

    public final com.snut_likelion.global.support.QBaseEntity _super = new com.snut_likelion.global.support.QBaseEntity(this);

    public final com.snut_likelion.domain.user.entity.QUser author;

    public final EnumPath<Category> category = createEnum("category", Category.class);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final ListPath<BlogImage, QBlogImage> images = this.<BlogImage, QBlogImage>createList("images", BlogImage.class, QBlogImage.class, PathInits.DIRECT2);

    public final EnumPath<PostStatus> status = createEnum("status", PostStatus.class);

    public final SetPath<com.snut_likelion.domain.user.entity.User, com.snut_likelion.domain.user.entity.QUser> taggedMembers = this.<com.snut_likelion.domain.user.entity.User, com.snut_likelion.domain.user.entity.QUser>createSet("taggedMembers", com.snut_likelion.domain.user.entity.User.class, com.snut_likelion.domain.user.entity.QUser.class, PathInits.DIRECT2);

    public final StringPath thumbnailUrl = createString("thumbnailUrl");

    public final StringPath title = createString("title");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QBlogPost(String variable) {
        this(BlogPost.class, forVariable(variable), INITS);
    }

    public QBlogPost(Path<? extends BlogPost> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBlogPost(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBlogPost(PathMetadata metadata, PathInits inits) {
        this(BlogPost.class, metadata, inits);
    }

    public QBlogPost(Class<? extends BlogPost> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.author = inits.isInitialized("author") ? new com.snut_likelion.domain.user.entity.QUser(forProperty("author"), inits.get("author")) : null;
    }

}

