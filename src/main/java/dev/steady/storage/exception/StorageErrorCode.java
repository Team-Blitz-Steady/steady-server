package dev.steady.storage.exception;

import dev.steady.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StorageErrorCode implements ErrorCode {

    NOT_SUPPORTED_FILE_TYPE("ST01", "지원하지 않는 파일 유형입니다."),
    NOT_SUPPORTED_PURPOSE("ST02", "지원하지 않는 용도입니다.");

    private final String code;
    private final String message;

    @Override
    public String code() {
        return this.code;
    }

    @Override
    public String message() {
        return this.message;
    }

}
