package dev.steady.auth.exception;

import dev.steady.global.exception.BusinessException;
import dev.steady.global.exception.ErrorCode;

public class OAuthPlatformException extends BusinessException {

    private final ErrorCode errorCode;

    public OAuthPlatformException(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

}
