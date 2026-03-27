package com.asset.asset_backend.domains.snapshot;

import com.asset.asset_backend.BaseControllerTest;
import com.asset.asset_backend.common.fixture.TestFixture;
import com.asset.asset_backend.domains.auth.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("SnapshotController 통합 테스트")
class SnapshotControllerTest extends BaseControllerTest {

    private User userA;
    private User userB;

    @BeforeEach
    void setUp() {
        userA = userRepository.save(
                TestFixture.createUser(TestFixture.MEMBER_A_ID, passwordEncoder.encode(TestFixture.RAW_PASSWORD)));
        userB = userRepository.save(
                TestFixture.createUser(TestFixture.MEMBER_B_ID, passwordEncoder.encode(TestFixture.RAW_PASSWORD)));
    }

    // ─── GET /api/snapshots ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/snapshots")
    class GetSnapshots {

        @Test
        @DisplayName("기본 period(30d) 조회 → 200, 본인 데이터만")
        void success_defaultPeriod() throws Exception {
            snapshotRepository.save(TestFixture.createSnapshot(userA, LocalDate.now().minusDays(5)));
            snapshotRepository.save(TestFixture.createSnapshot(userB, LocalDate.now().minusDays(5)));  // userB 데이터

            mockMvc.perform(get("/api/snapshots")
                            .cookie(authCookie(userA)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(1));
        }

        @Test
        @DisplayName("period=7d 조회 → 200, 7일 이내 데이터만")
        void success_7d() throws Exception {
            snapshotRepository.save(TestFixture.createSnapshot(userA, LocalDate.now().minusDays(5)));
            snapshotRepository.save(TestFixture.createSnapshot(userA, LocalDate.now().minusDays(10)));

            mockMvc.perform(get("/api/snapshots")
                            .param("period", "7d")
                            .cookie(authCookie(userA)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(1));
        }

        @Test
        @DisplayName("period=90d 조회 → 200")
        void success_90d() throws Exception {
            snapshotRepository.save(TestFixture.createSnapshot(userA, LocalDate.now().minusDays(60)));

            mockMvc.perform(get("/api/snapshots")
                            .param("period", "90d")
                            .cookie(authCookie(userA)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(1));
        }

        @Test
        @DisplayName("period=1y 조회 → 200")
        void success_1y() throws Exception {
            snapshotRepository.save(TestFixture.createSnapshot(userA, LocalDate.now().minusMonths(6)));

            mockMvc.perform(get("/api/snapshots")
                            .param("period", "1y")
                            .cookie(authCookie(userA)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(1));
        }

        @Test
        @DisplayName("잘못된 period → 400")
        void invalidPeriod() throws Exception {
            mockMvc.perform(get("/api/snapshots")
                            .param("period", "invalid")
                            .cookie(authCookie(userA)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("데이터 없으면 빈 목록 → 200")
        void emptyList() throws Exception {
            mockMvc.perform(get("/api/snapshots")
                            .cookie(authCookie(userA)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("인증 없이 접근 → 401")
        void unauthorized() throws Exception {
            mockMvc.perform(get("/api/snapshots"))
                    .andExpect(status().isUnauthorized());
        }
    }
}