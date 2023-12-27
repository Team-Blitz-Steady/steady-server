package dev.steady.global.advice;

import dev.steady.auth.exception.OAuthPlatformException;
import dev.steady.global.exception.AuthenticationException;
import dev.steady.global.exception.BusinessException;
import dev.steady.global.exception.DuplicateException;
import dev.steady.global.exception.ErrorCode;
import dev.steady.global.exception.ForbiddenException;
import dev.steady.global.exception.GlobalErrorCode;
import dev.steady.global.exception.InvalidStateException;
import dev.steady.global.exception.InvalidValueException;
import dev.steady.global.exception.NotFoundException;
import dev.steady.global.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handlerException(Exception e) {
        log.info("예상치 못한 서버 예외 발생", e);
        return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(GlobalErrorCode.INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler({InvalidStateException.class, InvalidValueException.class, DuplicateException.class})
    public ResponseEntity<ErrorResponse> handlerException(BusinessException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        log.info(errorCode.message(), exception);
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of(exception.getErrorCode()));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handlerException(AuthenticationException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        log.info(errorCode.message(), exception);
        return ResponseEntity.status(UNAUTHORIZED)
                .body(ErrorResponse.of(exception.getErrorCode()));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handlerException(ForbiddenException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        log.info(errorCode.message(), exception);
        return ResponseEntity.status(FORBIDDEN)
                .body(ErrorResponse.of(exception.getErrorCode()));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handlerException(NotFoundException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        log.info(errorCode.message(), exception);
        return ResponseEntity.status(NOT_FOUND)
                .body(ErrorResponse.of(exception.getErrorCode()));
    }


    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handlerException(OAuthPlatformException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        log.info(errorCode.message(), exception);
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of(exception.getErrorCode()));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handlerException(MethodArgumentNotValidException exception) {
        log.info(exception.getMessage(), exception.getBindingResult());
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of(GlobalErrorCode.INPUT_VALIDATION_ERROR, exception.getBindingResult()));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handlerException(MethodArgumentTypeMismatchException exception) {
        log.info(exception.getMessage(), exception);
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of(exception));
    }

}
