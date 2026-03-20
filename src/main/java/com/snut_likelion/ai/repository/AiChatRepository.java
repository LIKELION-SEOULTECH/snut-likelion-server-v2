package com.snut_likelion.ai.repository;

public interface AiChatRepository {

    ChatQueryResult chat(String text);

    record ChatQueryResult(String matchedQuestion, Double score) {
    }
}
