package com.asset.asset_backend.common.exception;


import com.asset.asset_backend.common.response.ApiResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // BaseException 처리
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResult<Void>> handleBaseException(BaseException ex) {
        log.error("BaseException: code={}, message={}, details={}",
                ex.getErrorCode().getCode(),
                ex.getErrorCode().getMessage(),
                ex.getDetails());

        ApiResult<Void> response = ApiResult.error(
                ex.getErrorCode(),
                ex.getDetails()
        );

        return ResponseEntity
                .status(ex.getErrorCode().getHttpStatus())
                .body(response);
    }

    // 유효성 검증 실패 (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResult<Void>> handleValidationException(
            MethodArgumentNotValidException ex) {

        // 첫 번째 에러만 사용
        String details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ApiResult<Void> response = ApiResult.error(
                ErrorCode.VALIDATION_ERROR,
                details
        );

        return ResponseEntity
                .status(ErrorCode.VALIDATION_ERROR.getHttpStatus())
                .body(response);
    }

    // 모든 예외 처리 (fallback)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<Void>> handleException(Exception ex) {
        log.error("Exception: {}", ex.getMessage(), ex);

        ApiResult<Void> response = ApiResult.error(
                ErrorCode.INTERNAL_SERVER_ERROR,
                ex.getMessage()
        );

        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(response);
    }
}