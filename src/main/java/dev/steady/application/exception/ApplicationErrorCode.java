package dev.steady.application.exception;

import dev.steady.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApplicationErrorCode implements ErrorCode {

    APPLICATION_NOT_FOUND("AP01", "신청서를 찾을 수 없습니다."),
    APPLICATION_AUTH_FAILURE("AP02", "신청서에 접근 권한이 없습니다."),
    STEADY_LEADER_SUBMISSION("AP03", "스테디 리더는 신청서를 제출할 수 없습니다."),
    APPLICATION_DUPLICATION("AP04","신청서는 중복 제출이 불가능합니다.");

    private final String errorCode;
    private final String message;

    @Override
    public String code() {
        return this.errorCode;
    }

    @Override
    public String message() {
        return this.message;
    }

}
