package com.snut_likelion.domain.recruitment.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QApplication is a Querydsl query type for Application
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QApplication extends EntityPathBase<Application> {

    private static final long serialVersionUID = 1503551198L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QApplication application = new QApplication("application");

    public final com.snut_likelion.global.support.QBaseEntity _super = new com.snut_likelion.global.support.QBaseEntity(this);

    public final ListPath<Answer, QAnswer> answers = this.<Answer, QAnswer>createList("answers", Answer.class, QAnswer.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final EnumPath<DepartmentType> departmentType = createEnum("departmentType", DepartmentType.class);

    public final NumberPath<Integer> grade = createNumber("grade", Integer.class);

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final BooleanPath inSchool = createBoolean("inSchool");

    public final BooleanPath isPersonalInfoConsent = createBoolean("isPersonalInfoConsent");

    public final StringPath major = createString("major");

    public final EnumPath<com.snut_likelion.domain.user.entity.Part> part = createEnum("part", com.snut_likelion.domain.user.entity.Part.class);

    public final StringPath portfolioName = createString("portfolioName");

    public final QRecruitment recruitment;

    public final EnumPath<ApplicationStatus> status = createEnum("status", ApplicationStatus.class);

    public final StringPath studentId = createString("studentId");

    public final DateTimePath<java.time.LocalDateTime> submittedAt = createDateTime("submittedAt", java.time.LocalDateTime.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final com.snut_likelion.domain.user.entity.QUser user;

    public QApplication(String variable) {
        this(Application.class, forVariable(variable), INITS);
    }

    public QApplication(Path<? extends Application> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QApplication(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QApplication(PathMetadata metadata, PathInits inits) {
        this(Application.class, metadata, inits);
    }

    public QApplication(Class<? extends Application> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.recruitment = inits.isInitialized("recruitment") ? new QRecruitment(forProperty("recruitment")) : null;
        this.user = inits.isInitialized("user") ? new com.snut_likelion.domain.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

