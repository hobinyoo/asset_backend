package com.asset.asset_backend.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON-001", "서버 오류가 발생했습니다"),
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON-002", "잘못된 입력입니다"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON-003", "리소스를 찾을 수 없습니다"),
    ALREADY_EXIST(HttpStatus.CONFLICT, "COMMON-004", "이미 존재하는 리소스입니다"),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "COMMON-005", "유효성 검증 실패");  // 👈 추가

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}