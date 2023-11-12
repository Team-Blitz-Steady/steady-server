package dev.steady.auth.exception;

import dev.steady.global.exception.BusinessException;
import dev.steady.global.exception.ErrorCode;

public class DuplicateRequestException extends BusinessException {

    private final ErrorCode errorCode;

    public DuplicateRequestException(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

}
