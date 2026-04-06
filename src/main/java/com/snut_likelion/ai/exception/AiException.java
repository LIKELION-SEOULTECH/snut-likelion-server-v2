package com.snut_likelion.ai.exception;

import com.snut_likelion.global.error.BaseError;
import com.snut_likelion.global.error.exception.SnutLikeLionException;
import org.springframework.http.HttpStatus;

public class AiException extends SnutLikeLionException {

    public AiException(BaseError error) {
        super(error, HttpStatus.SERVICE_UNAVAILABLE);
    }
}
