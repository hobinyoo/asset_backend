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
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "COMMON-005", "유효성 검증 실패"),

    // 인증
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH-001", "인증에 실패했습니다"),
    DUPLICATE_LOGIN_ID(HttpStatus.CONFLICT, "AUTH-002", "이미 사용 중인 아이디입니다"),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "AUTH-003", "비밀번호가 올바르지 않습니다"),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH-004", "존재하지 않는 회원입니다"),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-005", "유효하지 않은 리프레시 토큰입니다"),

    // 권한
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON-006", "접근 권한이 없습니다");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}