package com.snut_likelion.domain.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPortfolioLink is a Querydsl query type for PortfolioLink
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPortfolioLink extends EntityPathBase<PortfolioLink> {

    private static final long serialVersionUID = -1910645069L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPortfolioLink portfolioLink = new QPortfolioLink("portfolioLink");

    public final com.snut_likelion.global.support.QBaseEntity _super = new com.snut_likelion.global.support.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final EnumPath<PortFolioLinkType> name = createEnum("name", PortFolioLinkType.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final StringPath url = createString("url");

    public final QUser user;

    public QPortfolioLink(String variable) {
        this(PortfolioLink.class, forVariable(variable), INITS);
    }

    public QPortfolioLink(Path<? extends PortfolioLink> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPortfolioLink(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPortfolioLink(PathMetadata metadata, PathInits inits) {
        this(PortfolioLink.class, metadata, inits);
    }

    public QPortfolioLink(Class<? extends PortfolioLink> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user"), inits.get("user")) : null;
    }

}

