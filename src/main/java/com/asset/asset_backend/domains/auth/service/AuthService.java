package com.asset.asset_backend.domains.auth.service;

import com.asset.asset_backend.common.exception.BaseException;
import com.asset.asset_backend.common.exception.ErrorCode;
import com.asset.asset_backend.domains.auth.dto.request.LoginRequest;
import com.asset.asset_backend.domains.auth.dto.request.SignupRequest;
import com.asset.asset_backend.domains.auth.entity.RefreshToken;
import com.asset.asset_backend.domains.auth.entity.User;
import com.asset.asset_backend.domains.auth.jwt.JwtProvider;
import com.asset.asset_backend.domains.auth.repository.RefreshTokenRepository;
import com.asset.asset_backend.domains.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public User signup(SignupRequest request) {
        if (userRepository.existsByLoginId(request.getLoginId())) {
            throw new BaseException(ErrorCode.DUPLICATE_LOGIN_ID, "loginId: " + request.getLoginId());
        }
        User user = User.createUser(
                request.getLoginId(),
                passwordEncoder.encode(request.getPassword())
        );
        return userRepository.save(user);
    }

    @Transactional
    public TokenResult login(LoginRequest request) {
        User user = userRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND, "loginId: " + request.getLoginId()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BaseException(ErrorCode.INVALID_PASSWORD, "loginId: " + request.getLoginId());
        }

        String accessToken = jwtProvider.generateAccessToken(user.getLoginId(), user.getId());
        String refreshTokenValue = jwtProvider.generateRefreshToken(user.getLoginId());

        refreshTokenRepository.deleteByUser(user);
        refreshTokenRepository.save(
                RefreshToken.createRefreshToken(user, refreshTokenValue, jwtProvider.getRefreshTokenExpiredAt())
        );


        return new TokenResult(accessToken, refreshTokenValue);
    }

    public String refresh(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new BaseException(ErrorCode.INVALID_REFRESH_TOKEN, "토큰이 존재하지 않습니다"));

        if (refreshToken.isExpired() || !jwtProvider.validateToken(refreshTokenValue)) {
            throw new BaseException(ErrorCode.INVALID_REFRESH_TOKEN, "만료된 리프레시 토큰입니다");
        }

        User user = refreshToken.getUser();
        return jwtProvider.generateAccessToken(user.getLoginId(), user.getId());
    }

    @Transactional
    public void logout(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND, "User ID: " + userId));
        refreshTokenRepository.deleteByUser(user);
    }

    public User getMe(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND, "User ID: " + userId));
    }

    public record TokenResult(String accessToken, String refreshToken) {}
}
