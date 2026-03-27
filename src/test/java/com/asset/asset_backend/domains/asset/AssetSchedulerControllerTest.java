package com.asset.asset_backend.domains.asset;

import com.asset.asset_backend.BaseControllerTest;
import com.asset.asset_backend.common.fixture.TestFixture;
import com.asset.asset_backend.domains.auth.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("AssetSchedulerController 통합 테스트")
class AssetSchedulerControllerTest extends BaseControllerTest {

    private User userA;

    @BeforeEach
    void setUp() {
        userA = userRepository.save(
                TestFixture.createUser(TestFixture.MEMBER_A_ID, passwordEncoder.encode(TestFixture.RAW_PASSWORD)));
    }

    // ─── POST /api/admin/scheduler/daily-snapshot ────────────────────────────────

    @Nested
    @DisplayName("POST /api/admin/scheduler/daily-snapshot")
    class RunDailySnapshot {

        @Test
        @DisplayName("인증된 사용자 → 200, saveDailySnapshot() 호출")
        void success() throws Exception {
            mockMvc.perform(post("/api/admin/scheduler/daily-snapshot")
                            .cookie(authCookie(userA)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("스냅샷 저장 완료"));

            verify(assetScheduler, times(1)).saveDailySnapshot();
        }

        @Test
        @DisplayName("인증 없이 접근 → 401")
        void unauthorized() throws Exception {
            mockMvc.perform(post("/api/admin/scheduler/daily-snapshot"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─── POST /api/admin/scheduler/payment ───────────────────────────────────────

    @Nested
    @DisplayName("POST /api/admin/scheduler/payment")
    class RunPayment {

        @Test
        @DisplayName("인증된 사용자 → 200, processMonthlyPayments() 호출")
        void success() throws Exception {
            mockMvc.perform(post("/api/admin/scheduler/payment")
                            .cookie(authCookie(userA)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("자산 납입 스케줄러 실행 완료"));

            verify(assetPaymentScheduler, times(1)).processMonthlyPayments();
        }

        @Test
        @DisplayName("인증 없이 접근 → 401")
        void unauthorized() throws Exception {
            mockMvc.perform(post("/api/admin/scheduler/payment"))
                    .andExpect(status().isUnauthorized());
        }
    }
}