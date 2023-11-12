package dev.steady.global.exception;

public class AuthenticationException extends BusinessException {

    private final ErrorCode errorCode;

    public AuthenticationException(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

}
