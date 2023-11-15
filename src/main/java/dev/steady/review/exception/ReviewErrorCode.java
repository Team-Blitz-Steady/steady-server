package dev.steady.review.exception;

import dev.steady.global.exception.ErrorCode;

public enum ReviewErrorCode implements ErrorCode {

    REVIEW_NOT_FOUND("R001", "리뷰를 찾을 수 없습니다."),
    CARD_NOT_FOUND("R002", "카드를 찾을 수 없습니다."),
    STEADY_NOT_FINISHED("R003", "스테디가 종료되지 않았습니다."),
    REVIEW_NOT_ENABLED_PERIOD("R004", "리뷰할 수 있는 기간이 아닙니다."),
    REVIEWEE_EQUALS_REVIEWER("R005", "리뷰이와 리뷰어가 동일합니다."),
    REVIEW_DUPLICATE("R006", "리뷰를 중복 제출할 수 없습니다.");
    
    private final String code;
    private final String message;

    ReviewErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }

}
