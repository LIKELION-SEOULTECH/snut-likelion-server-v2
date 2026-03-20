package com.snut_likelion.domain.recruitment.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum QuestionTarget {
    DEFAULT("기본 질문"), // 개인정보 질문 (이름, 학과, 학번, 핸드폰 번호, 학년, 학적 상태)
    COMMON("공통 질문"), // 공통 질문
    PART("파트 질문"), // 파트 질문
    DEPARTMENT("부서 질문"), // 부서 질문
    ;

    private final String description;
}
