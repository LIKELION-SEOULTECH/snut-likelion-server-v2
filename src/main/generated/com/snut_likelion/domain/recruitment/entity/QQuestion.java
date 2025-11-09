package com.snut_likelion.domain.recruitment.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QQuestion is a Querydsl query type for Question
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QQuestion extends EntityPathBase<Question> {

    private static final long serialVersionUID = 1717239064L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QQuestion question = new QQuestion("question");

    public final com.snut_likelion.global.support.QBaseEntity _super = new com.snut_likelion.global.support.QBaseEntity(this);

    public final StringPath buttonList = createString("buttonList");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final EnumPath<DepartmentType> departmentType = createEnum("departmentType", DepartmentType.class);

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final NumberPath<Integer> orderNum = createNumber("orderNum", Integer.class);

    public final EnumPath<com.snut_likelion.domain.user.entity.Part> part = createEnum("part", com.snut_likelion.domain.user.entity.Part.class);

    public final EnumPath<QuestionTarget> questionTarget = createEnum("questionTarget", QuestionTarget.class);

    public final EnumPath<QuestionType> questionType = createEnum("questionType", QuestionType.class);

    public final QRecruitment recruitment;

    public final StringPath text = createString("text");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QQuestion(String variable) {
        this(Question.class, forVariable(variable), INITS);
    }

    public QQuestion(Path<? extends Question> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QQuestion(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QQuestion(PathMetadata metadata, PathInits inits) {
        this(Question.class, metadata, inits);
    }

    public QQuestion(Class<? extends Question> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.recruitment = inits.isInitialized("recruitment") ? new QRecruitment(forProperty("recruitment")) : null;
    }

}

