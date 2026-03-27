package com.asset.asset_backend.domains.auth;

import com.asset.asset_backend.BaseControllerTest;
import com.asset.asset_backend.common.fixture.TestFixture;
import com.asset.asset_backend.domains.auth.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("AuthController 통합 테스트")
class AuthControllerTest extends BaseControllerTest {

    // ─── POST /api/auth/signup ───────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/auth/signup")
    class Signup {

        @Test
        @DisplayName("정상 회원가입 → 201")
        void success() throws Exception {
            String body = objectMapper.writeValueAsString(
                    Map.of("loginId", "newuser", "password", "pass1234"));

            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.loginId").value("newuser"));
        }

        @Test
        @DisplayName("중복 loginId → 409")
        void duplicateLoginId() throws Exception {
            userRepository.save(TestFixture.createUser("existing", passwordEncoder.encode("pass")));
            String body = objectMapper.writeValueAsString(
                    Map.of("loginId", "existing", "password", "pass1234"));

            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("빈 loginId (@NotBlank 위반) → 400")
        void blankLoginId() throws Exception {
            String body = objectMapper.writeValueAsString(
                    Map.of("loginId", "", "password", "pass1234"));

            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }
    }

    // ─── POST /api/auth/login ────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/auth/login")
    class Login {

        @Test
        @DisplayName("정상 로그인 → 200 + Set-Cookie")
        void success() throws Exception {
            userRepository.save(TestFixture.createUser("testuser", passwordEncoder.encode("pass1234")));
            String body = objectMapper.writeValueAsString(
                    Map.of("loginId", "testuser", "password", "pass1234"));

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(header().exists("Set-Cookie"));
        }

        @Test
        @DisplayName("틀린 비밀번호 → 401")
        void wrongPassword() throws Exception {
            userRepository.save(TestFixture.createUser("testuser", passwordEncoder.encode("pass1234")));
            String body = objectMapper.writeValueAsString(
                    Map.of("loginId", "testuser", "password", "wrongpass"));

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("존재하지 않는 회원 → 404")
        void memberNotFound() throws Exception {
            String body = objectMapper.writeValueAsString(
                    Map.of("loginId", "nobody", "password", "pass1234"));

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isNotFound());
        }
    }

    // ─── POST /api/auth/refresh ──────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/auth/refresh")
    class Refresh {

        @Test
        @DisplayName("유효한 refresh_token → 200 + 새 access_token Set-Cookie")
        void success() throws Exception {
            User user = userRepository.save(
                    TestFixture.createUser("testuser", passwordEncoder.encode("pass")));
            String refreshToken = jwtProvider.generateRefreshToken("testuser");
            refreshTokenRepository.save(TestFixture.createRefreshToken(user, refreshToken));

            mockMvc.perform(post("/api/auth/refresh")
                            .cookie(new jakarta.servlet.http.Cookie("refresh_token", refreshToken)))
                    .andExpect(status().isOk())
                    .andExpect(header().exists("Set-Cookie"));
        }

        @Test
        @DisplayName("refresh_token 쿠키 없음 → 401")
        void noCookie() throws Exception {
            mockMvc.perform(post("/api/auth/refresh"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─── POST /api/auth/logout ───────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/auth/logout")
    class Logout {

        @Test
        @DisplayName("정상 로그아웃 → 200")
        void success() throws Exception {
            User user = userRepository.save(
                    TestFixture.createUser("testuser", passwordEncoder.encode("pass")));

            mockMvc.perform(post("/api/auth/logout")
                            .cookie(authCookie(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("인증 없이 접근 → 401")
        void unauthorized() throws Exception {
            mockMvc.perform(post("/api/auth/logout"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─── GET /api/auth/me ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/auth/me")
    class GetMe {

        @Test
        @DisplayName("정상 내 정보 조회 → 200")
        void success() throws Exception {
            User user = userRepository.save(
                    TestFixture.createUser("testuser", passwordEncoder.encode("pass")));

            mockMvc.perform(get("/api/auth/me")
                            .cookie(authCookie(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.loginId").value("testuser"));
        }

        @Test
        @DisplayName("인증 없이 접근 → 401")
        void unauthorized() throws Exception {
            mockMvc.perform(get("/api/auth/me"))
                    .andExpect(status().isUnauthorized());
        }
    }
}