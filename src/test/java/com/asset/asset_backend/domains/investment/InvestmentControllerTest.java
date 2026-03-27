package com.asset.asset_backend.domains.investment;

import com.asset.asset_backend.BaseControllerTest;
import com.asset.asset_backend.common.fixture.TestFixture;
import com.asset.asset_backend.domains.asset.entity.Asset;
import com.asset.asset_backend.domains.auth.entity.User;
import com.asset.asset_backend.domains.investment.entity.Investment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("InvestmentController 통합 테스트")
class InvestmentControllerTest extends BaseControllerTest {

    private User userA;
    private User userB;
    private Asset assetA;

    @BeforeEach
    void setUp() {
        userA = userRepository.save(
                TestFixture.createUser(TestFixture.MEMBER_A_ID, passwordEncoder.encode(TestFixture.RAW_PASSWORD)));
        userB = userRepository.save(
                TestFixture.createUser(TestFixture.MEMBER_B_ID, passwordEncoder.encode(TestFixture.RAW_PASSWORD)));
        assetA = assetRepository.save(TestFixture.createLinkedAsset(userA));

        when(stockPriceService.getCurrentPrice(anyString())).thenReturn(50_000L);
        when(exchangeRateService.getUsdToKrw()).thenReturn(1350.0);
    }

    private String investmentBody(Long assetId) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "assetId", assetId,
                "category", "ETF",
                "stockName", "S&P500",
                "ticker", "SPY",
                "owner", "본인",
                "purchasePrice", 50_000,
                "quantity", 10,
                "purchaseAmount", 500_000,
                "marketType", "OVERSEAS"
        ));
    }

    // ─── POST /api/investments ───────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/investments")
    class CreateInvestment {

        @Test
        @DisplayName("정상 투자 생성 → 201")
        void success() throws Exception {
            mockMvc.perform(post("/api/investments")
                            .cookie(authCookie(userA))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(investmentBody(assetA.getId())))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.stockName").value("S&P500"));
        }

        @Test
        @DisplayName("인증 없이 접근 → 401")
        void unauthorized() throws Exception {
            mockMvc.perform(post("/api/investments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(investmentBody(assetA.getId())))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("다른 유저의 자산에 투자 생성 → 403")
        void forbidden_otherMemberAsset() throws Exception {
            mockMvc.perform(post("/api/investments")
                            .cookie(authCookie(userB))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(investmentBody(assetA.getId())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("존재하지 않는 assetId → 404")
        void notFound_asset() throws Exception {
            mockMvc.perform(post("/api/investments")
                            .cookie(authCookie(userA))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(investmentBody(999_999L)))
                    .andExpect(status().isNotFound());
        }
    }

    // ─── GET /api/investments ────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/investments")
    class GetInvestments {

        @Test
        @DisplayName("정상 목록 조회 → 200, 본인 데이터만")
        void success() throws Exception {
            Asset assetB = assetRepository.save(TestFixture.createLinkedAsset(userB));
            investmentRepository.save(TestFixture.createInvestment(assetA));
            investmentRepository.save(TestFixture.createInvestment(assetB));   // userB 데이터

            mockMvc.perform(get("/api/investments")
                            .cookie(authCookie(userA)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content.length()").value(1));
        }

        @Test
        @DisplayName("인증 없이 접근 → 401")
        void unauthorized() throws Exception {
            mockMvc.perform(get("/api/investments"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─── GET /api/investments/{id} ───────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/investments/{id}")
    class GetInvestmentById {

        @Test
        @DisplayName("정상 단건 조회 → 200")
        void success() throws Exception {
            Investment investment = investmentRepository.save(TestFixture.createInvestment(assetA));

            mockMvc.perform(get("/api/investments/{id}", investment.getId())
                            .cookie(authCookie(userA)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(investment.getId()));
        }

        @Test
        @DisplayName("인증 없이 접근 → 401")
        void unauthorized() throws Exception {
            Investment investment = investmentRepository.save(TestFixture.createInvestment(assetA));

            mockMvc.perform(get("/api/investments/{id}", investment.getId()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("다른 유저의 투자 접근 → 403")
        void forbidden() throws Exception {
            Investment investment = investmentRepository.save(TestFixture.createInvestment(assetA));

            mockMvc.perform(get("/api/investments/{id}", investment.getId())
                            .cookie(authCookie(userB)))
                    .andExpect(status().isForbidden());
        }
    }

    // ─── GET /api/investments/asset/{assetId} ────────────────────────────────────

    @Nested
    @DisplayName("GET /api/investments/asset/{assetId}")
    class GetInvestmentsByAssetId {

        @Test
        @DisplayName("정상 자산별 투자 조회 → 200")
        void success() throws Exception {
            investmentRepository.save(TestFixture.createInvestment(assetA));

            mockMvc.perform(get("/api/investments/asset/{assetId}", assetA.getId())
                            .cookie(authCookie(userA)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(1));
        }

        @Test
        @DisplayName("인증 없이 접근 → 401")
        void unauthorized() throws Exception {
            mockMvc.perform(get("/api/investments/asset/{assetId}", assetA.getId()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("다른 유저의 자산 투자 목록 접근 → 403")
        void forbidden() throws Exception {
            mockMvc.perform(get("/api/investments/asset/{assetId}", assetA.getId())
                            .cookie(authCookie(userB)))
                    .andExpect(status().isForbidden());
        }
    }

    // ─── PUT /api/investments/{id} ───────────────────────────────────────────────

    @Nested
    @DisplayName("PUT /api/investments/{id}")
    class UpdateInvestment {

        @Test
        @DisplayName("정상 수정 → 200")
        void success() throws Exception {
            Investment investment = investmentRepository.save(TestFixture.createInvestment(assetA));
            String body = objectMapper.writeValueAsString(Map.of(
                    "stockName", "나스닥100", "ticker", "QQQ",
                    "category", "ETF", "owner", "본인",
                    "purchasePrice", 45_000, "quantity", 5,
                    "purchaseAmount", 225_000, "marketType", "OVERSEAS"));

            mockMvc.perform(put("/api/investments/{id}", investment.getId())
                            .cookie(authCookie(userA))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.stockName").value("나스닥100"));
        }

        @Test
        @DisplayName("인증 없이 접근 → 401")
        void unauthorized() throws Exception {
            Investment investment = investmentRepository.save(TestFixture.createInvestment(assetA));

            mockMvc.perform(put("/api/investments/{id}", investment.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("다른 유저의 투자 수정 → 403")
        void forbidden() throws Exception {
            Investment investment = investmentRepository.save(TestFixture.createInvestment(assetA));

            mockMvc.perform(put("/api/investments/{id}", investment.getId())
                            .cookie(authCookie(userB))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isForbidden());
        }
    }

    // ─── DELETE /api/investments/{id} ────────────────────────────────────────────

    @Nested
    @DisplayName("DELETE /api/investments/{id}")
    class DeleteInvestment {

        @Test
        @DisplayName("정상 삭제 → 200")
        void success() throws Exception {
            Investment investment = investmentRepository.save(TestFixture.createInvestment(assetA));

            mockMvc.perform(delete("/api/investments/{id}", investment.getId())
                            .cookie(authCookie(userA)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("인증 없이 접근 → 401")
        void unauthorized() throws Exception {
            Investment investment = investmentRepository.save(TestFixture.createInvestment(assetA));

            mockMvc.perform(delete("/api/investments/{id}", investment.getId()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("다른 유저의 투자 삭제 → 403")
        void forbidden() throws Exception {
            Investment investment = investmentRepository.save(TestFixture.createInvestment(assetA));

            mockMvc.perform(delete("/api/investments/{id}", investment.getId())
                            .cookie(authCookie(userB)))
                    .andExpect(status().isForbidden());
        }
    }

    // ─── POST /api/investments/sync-asset/{assetId} ──────────────────────────────

    @Nested
    @DisplayName("POST /api/investments/sync-asset/{assetId}")
    class SyncAssetAmount {

        @Test
        @DisplayName("정상 자산 금액 동기화 → 200")
        void success() throws Exception {
            investmentRepository.save(TestFixture.createInvestment(assetA));

            mockMvc.perform(post("/api/investments/sync-asset/{assetId}", assetA.getId())
                            .cookie(authCookie(userA)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("인증 없이 접근 → 401")
        void unauthorized() throws Exception {
            mockMvc.perform(post("/api/investments/sync-asset/{assetId}", assetA.getId()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("다른 유저의 자산 동기화 → 403")
        void forbidden() throws Exception {
            mockMvc.perform(post("/api/investments/sync-asset/{assetId}", assetA.getId())
                            .cookie(authCookie(userB)))
                    .andExpect(status().isForbidden());
        }
    }
}