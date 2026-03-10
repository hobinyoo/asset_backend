package com.asset.asset_backend.common.response;

import com.asset.asset_backend.common.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)

public class ApiResult<T> {
    private T data;
    private Boolean success;
    private Integer status;
    private String message;
    private ErrorDetail error;


    public static <T> ApiResult<T> success(T data, String message) {
        return ApiResult.<T>builder()
                .data(data)
                .success(true)
                .status(HttpStatus.OK.value())
                .message(message)
                .build();
    }

    // 에러 응답
    public static <T> ApiResult<T> error(ErrorCode errorCode, String details) {
        return ApiResult.<T>builder()
                .success(false)
                .status(errorCode.getHttpStatus().value())
                .message(errorCode.getMessage())
                .error(ErrorDetail.of(errorCode.getCode(), details))
                .build();
    }

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorDetail {
        private String code;
        private String details;

        public static ErrorDetail of(String code, String details) {
            return ErrorDetail.builder()
                    .code(code)
                    .details(details)
                    .build();
        }
    }
}