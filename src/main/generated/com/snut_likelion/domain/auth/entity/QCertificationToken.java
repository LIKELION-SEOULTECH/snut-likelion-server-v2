package com.snut_likelion.domain.auth.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QCertificationToken is a Querydsl query type for CertificationToken
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCertificationToken extends EntityPathBase<CertificationToken> {

    private static final long serialVersionUID = 2140082763L;

    public static final QCertificationToken certificationToken = new QCertificationToken("certificationToken");

    public final StringPath code = createString("code");

    public final StringPath email = createString("email");

    public final DateTimePath<java.time.LocalDateTime> expiredAt = createDateTime("expiredAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public QCertificationToken(String variable) {
        super(CertificationToken.class, forVariable(variable));
    }

    public QCertificationToken(Path<? extends CertificationToken> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCertificationToken(PathMetadata metadata) {
        super(CertificationToken.class, metadata);
    }

}

