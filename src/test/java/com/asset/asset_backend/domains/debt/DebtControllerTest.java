package com.asset.asset_backend.domains.debt;

import com.asset.asset_backend.BaseControllerTest;
import com.asset.asset_backend.common.fixture.TestFixture;
import com.asset.asset_backend.domains.auth.entity.Member;
import com.asset.asset_backend.domains.debt.entity.Debt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("DebtController 통합 테스트")
class DebtControllerTest extends BaseControllerTest {

    private Member memberA;
    private Member memberB;

    @BeforeEach
    void setUp() {
        memberA = memberRepository.save(
                TestFixture.createMember(TestFixture.MEMBER_A_ID, passwordEncoder.encode(TestFixture.RAW_PASSWORD)));
        memberB = memberRepository.save(
                TestFixture.createMember(TestFixture.MEMBER_B_ID, passwordEncoder.encode(TestFixture.RAW_PASSWORD)));
    }

    private String debtBody() throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "category", "신용대출",
                "owner", "본인",
                "amount", 5_000_000,
                "type", "SAVINGS"
        ));
    }

    // ─── POST /api/debts ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/debts")
    class CreateDebt {

        @Test
        @DisplayName("정상 부채 생성 → 201")
        void success() throws Exception {
            mockMvc.perform(post("/api/debts")
                            .cookie(authCookie(TestFixture.MEMBER_A_ID))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(debtBody()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.category").value("신용대출"));
        }

        @Test
        @DisplayName("인증 없이 접근 → 401")
        void unauthorized() throws Exception {
            mockMvc.perform(post("/api/debts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(debtBody()))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─── GET /api/debts ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/debts")
    class GetDebts {

        @Test
        @DisplayName("정상 목록 조회 → 200, 본인 데이터만")
        void success() throws Exception {
            // given
            debtRepository.save(TestFixture.createDebt(memberA));
            debtRepository.save(TestFixture.createDebt(memberB));   // memberB 데이터

            // when & then
            mockMvc.perform(get("/api/debts")
                            .cookie(authCookie(TestFixture.MEMBER_A_ID)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content.length()").value(1));
        }

        @Test
        @DisplayName("인증 없이 접근 → 401")
        void unauthorized() throws Exception {
            mockMvc.perform(get("/api/debts"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─── GET /api/debts/{id} ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/debts/{id}")
    class GetDebtById {

        @Test
        @DisplayName("정상 단건 조회 → 200")
        void success() throws Exception {
            // given
            Debt debt = debtRepository.save(TestFixture.createDebt(memberA));

            // when & then
            mockMvc.perform(get("/api/debts/{id}", debt.getId())
                            .cookie(authCookie(TestFixture.MEMBER_A_ID)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(debt.getId()));
        }

        @Test
        @DisplayName("인증 없이 접근 → 401")
        void unauthorized() throws Exception {
            Debt debt = debtRepository.save(TestFixture.createDebt(memberA));

            mockMvc.perform(get("/api/debts/{id}", debt.getId()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("다른 멤버의 부채 접근 → 403")
        void forbidden() throws Exception {
            // given
            Debt debt = debtRepository.save(TestFixture.createDebt(memberA));

            // when & then
            mockMvc.perform(get("/api/debts/{id}", debt.getId())
                            .cookie(authCookie(TestFixture.MEMBER_B_ID)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("존재하지 않는 ID → 404")
        void notFound() throws Exception {
            mockMvc.perform(get("/api/debts/{id}", 999_999L)
                            .cookie(authCookie(TestFixture.MEMBER_A_ID)))
                    .andExpect(status().isNotFound());
        }
    }

    // ─── PUT /api/debts/{id} ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("PUT /api/debts/{id}")
    class UpdateDebt {

        @Test
        @DisplayName("정상 수정 → 200")
        void success() throws Exception {
            // given
            Debt debt = debtRepository.save(TestFixture.createDebt(memberA));
            String body = objectMapper.writeValueAsString(Map.of(
                    "category", "주택담보대출", "owner", "공동",
                    "amount", 100_000_000, "type", "HOUSING"));

            // when & then
            mockMvc.perform(put("/api/debts/{id}", debt.getId())
                            .cookie(authCookie(TestFixture.MEMBER_A_ID))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.category").value("주택담보대출"));
        }

        @Test
        @DisplayName("인증 없이 접근 → 401")
        void unauthorized() throws Exception {
            Debt debt = debtRepository.save(TestFixture.createDebt(memberA));

            mockMvc.perform(put("/api/debts/{id}", debt.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(debtBody()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("다른 멤버의 부채 수정 → 403")
        void forbidden() throws Exception {
            // given
            Debt debt = debtRepository.save(TestFixture.createDebt(memberA));

            // when & then
            mockMvc.perform(put("/api/debts/{id}", debt.getId())
                            .cookie(authCookie(TestFixture.MEMBER_B_ID))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(debtBody()))
                    .andExpect(status().isForbidden());
        }
    }

    // ─── DELETE /api/debts/{id} ──────────────────────────────────────────────────

    @Nested
    @DisplayName("DELETE /api/debts/{id}")
    class DeleteDebt {

        @Test
        @DisplayName("정상 삭제 → 200")
        void success() throws Exception {
            // given
            Debt debt = debtRepository.save(TestFixture.createDebt(memberA));

            // when & then
            mockMvc.perform(delete("/api/debts/{id}", debt.getId())
                            .cookie(authCookie(TestFixture.MEMBER_A_ID)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("인증 없이 접근 → 401")
        void unauthorized() throws Exception {
            Debt debt = debtRepository.save(TestFixture.createDebt(memberA));

            mockMvc.perform(delete("/api/debts/{id}", debt.getId()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("다른 멤버의 부채 삭제 → 403")
        void forbidden() throws Exception {
            // given
            Debt debt = debtRepository.save(TestFixture.createDebt(memberA));

            // when & then
            mockMvc.perform(delete("/api/debts/{id}", debt.getId())
                            .cookie(authCookie(TestFixture.MEMBER_B_ID)))
                    .andExpect(status().isForbidden());
        }
    }

    // ─── GET /api/debts/summary ──────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/debts/summary")
    class GetSummary {

        @Test
        @DisplayName("정상 요약 조회 → 200")
        void success() throws Exception {
            // given
            debtRepository.save(TestFixture.createDebt(memberA));

            // when & then
            mockMvc.perform(get("/api/debts/summary")
                            .cookie(authCookie(TestFixture.MEMBER_A_ID)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalAmount").exists())
                    .andExpect(jsonPath("$.data.totalMonthlyPayment").exists());
        }

        @Test
        @DisplayName("인증 없이 접근 → 401")
        void unauthorized() throws Exception {
            mockMvc.perform(get("/api/debts/summary"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─── POST /api/debts/scheduler/payment ──────────────────────────────────────

    @Test
    @DisplayName("POST /api/debts/scheduler/payment - 수동 스케줄러 실행 → 200")
    void triggerScheduler_success() throws Exception {
        mockMvc.perform(post("/api/debts/scheduler/payment")
                        .cookie(authCookie(TestFixture.MEMBER_A_ID)))
                .andExpect(status().isOk());
    }
}
