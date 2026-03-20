package com.snut_likelion.ai.repository;

import java.util.Optional;

public interface IntentAnswerPort {

    Optional<String> findAnswer(String intent);
}
