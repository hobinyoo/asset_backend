package com.asset.asset_backend.domains.auth.controller;

import com.asset.asset_backend.common.exception.BaseException;
import com.asset.asset_backend.common.exception.ErrorCode;
import com.asset.asset_backend.common.response.ApiResult;
import com.asset.asset_backend.domains.auth.dto.request.LoginRequest;
import com.asset.asset_backend.domains.auth.dto.request.SignupRequest;
import com.asset.asset_backend.domains.auth.dto.response.UserResponse;
import com.asset.asset_backend.domains.auth.entity.User;
import com.asset.asset_backend.domains.auth.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.WebUtils;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResult<UserResponse>> signup(@Valid @RequestBody SignupRequest request) {
        User user = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResult.success(UserResponse.from(user), "회원가입이 완료되었습니다."));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResult<Void>> login(@Valid @RequestBody LoginRequest request,
                                                  HttpServletResponse response) {
        AuthService.TokenResult result = authService.login(request);
        addCookie(response, "access_token",  result.accessToken(),  Duration.ofMinutes(15));
        addCookie(response, "refresh_token", result.refreshToken(), Duration.ofDays(7));
        return ResponseEntity.ok(ApiResult.success(null, "로그인이 완료되었습니다."));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResult<Void>> refresh(HttpServletRequest request,
                                                    HttpServletResponse response) {
        Cookie cookie = WebUtils.getCookie(request, "refresh_token");
        if (cookie == null) {
            throw new BaseException(ErrorCode.INVALID_REFRESH_TOKEN, "리프레시 토큰 쿠키가 없습니다");
        }
        String newAccessToken = authService.refresh(cookie.getValue());
        addCookie(response, "access_token", newAccessToken, Duration.ofMinutes(15));
        return ResponseEntity.ok(ApiResult.success(null, "AccessToken이 재발급되었습니다."));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResult<Void>> logout(@AuthenticationPrincipal Long userId,
                                                   HttpServletResponse response) {
        authService.logout(userId);
        clearCookie(response, "access_token");
        clearCookie(response, "refresh_token");
        return ResponseEntity.ok(ApiResult.success(null, "로그아웃되었습니다."));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResult<UserResponse>> getMe(@AuthenticationPrincipal Long userId) {
        User user = authService.getMe(userId);
        return ResponseEntity.ok(ApiResult.success(UserResponse.from(user), "내 정보를 조회했습니다."));
    }

    private void addCookie(HttpServletResponse response,
                           String name,
                           String value,
                           Duration maxAge) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .maxAge(maxAge)
                .path("/")
                .domain(".yoojoo-asset-management.xyz")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearCookie(HttpServletResponse response, String name) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .maxAge(Duration.ZERO)
                .path("/")
                .domain(".yoojoo-asset-management.xyz")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
