package com.asset.asset_backend.domains.asset;

import com.asset.asset_backend.BaseControllerTest;
import com.asset.asset_backend.common.fixture.TestFixture;
import com.asset.asset_backend.domains.asset.entity.Asset;
import com.asset.asset_backend.domains.auth.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("AssetController 통합 테스트")
class AssetControllerTest extends BaseControllerTest {

    private User userA;
    private User userB;

    @BeforeEach
    void setUp() {
        userA = userRepository.save(
                TestFixture.createUser(TestFixture.MEMBER_A_ID, passwordEncoder.encode(TestFixture.RAW_PASSWORD)));
        userB = userRepository.save(
                TestFixture.createUser(TestFixture.MEMBER_B_ID, passwordEncoder.encode(TestFixture.RAW_PASSWORD)));
    }

    private String assetBody() throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "category", "청약저축",
                "owner", "본인",
                "amount", 10_000_000,
                "type", "SAVINGS"
        ));
    }

    // ─── POST /api/assets ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/assets")
    class CreateAsset {

        @Test
        @DisplayName("정상 자산 생성 → 201")
        void success() throws Exception {
            mockMvc.perform(post("/api/assets")
                            .cookie(authCookie(userA))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(assetBody()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.category").value("청약저축"));
        }

        @Test
        @DisplayName("인증 없이 접근 → 401")
        void unauthorized() throws Exception {
            mockMvc.perform(post("/api/assets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(assetBody()))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─── GET /api/assets ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/assets")
    class GetAssets {

        @Test
        @DisplayName("정상 목록 조회 → 200, 본인 데이터만")
        void success() throws Exception {
            assetRepository.save(TestFixture.createAsset(userA));
            assetRepository.save(TestFixture.createAsset(userB));   // userB 데이터

            mockMvc.perform(get("/api/assets")
                            .cookie(authCookie(userA)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content.length()").value(1));
        }

        @Test
        @DisplayName("인증 없이 접근 → 401")
        void unauthorized() throws Exception {
            mockMvc.perform(get("/api/assets"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─── GET /api/assets/{id} ────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/assets/{id}")
    class GetAssetById {

        @Test
        @DisplayName("정상 단건 조회 → 200")
        void success() throws Exception {
            Asset asset = assetRepository.save(TestFixture.createAsset(userA));

            mockMvc.perform(get("/api/assets/{id}", asset.getId())
                            .cookie(authCookie(userA)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(asset.getId()));
        }

        @Test
        @DisplayName("인증 없이 접근 → 401")
        void unauthorized() throws Exception {
            Asset asset = assetRepository.save(TestFixture.createAsset(userA));

            mockMvc.perform(get("/api/assets/{id}", asset.getId()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("다른 유저의 자산 접근 → 403")
        void forbidden() throws Exception {
            Asset asset = assetRepository.save(TestFixture.createAsset(userA));

            mockMvc.perform(get("/api/assets/{id}", asset.getId())
                            .cookie(authCookie(userB)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("존재하지 않는 ID → 404")
        void notFound() throws Exception {
            mockMvc.perform(get("/api/assets/{id}", 999_999L)
                            .cookie(authCookie(userA)))
                    .andExpect(status().isNotFound());
        }
    }

    // ─── PUT /api/assets/{id} ────────────────────────────────────────────────────

    @Nested
    @DisplayName("PUT /api/assets/{id}")
    class UpdateAsset {

        @Test
        @DisplayName("정상 수정 → 200")
        void success() throws Exception {
            Asset asset = assetRepository.save(TestFixture.createAsset(userA));
            String body = objectMapper.writeValueAsString(Map.of(
                    "category", "IRP계좌", "owner", "본인",
                    "amount", 20_000_000, "type", "RETIREMENT"));

            mockMvc.perform(put("/api/assets/{id}", asset.getId())
                            .cookie(authCookie(userA))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.category").value("IRP계좌"));
        }

        @Test
        @DisplayName("인증 없이 접근 → 401")
        void unauthorized() throws Exception {
            Asset asset = assetRepository.save(TestFixture.createAsset(userA));

            mockMvc.perform(put("/api/assets/{id}", asset.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(assetBody()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("다른 유저의 자산 수정 → 403")
        void forbidden() throws Exception {
            Asset asset = assetRepository.save(TestFixture.createAsset(userA));

            mockMvc.perform(put("/api/assets/{id}", asset.getId())
                            .cookie(authCookie(userB))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(assetBody()))
                    .andExpect(status().isForbidden());
        }
    }

    // ─── DELETE /api/assets/{id} ─────────────────────────────────────────────────

    @Nested
    @DisplayName("DELETE /api/assets/{id}")
    class DeleteAsset {

        @Test
        @DisplayName("정상 삭제 → 200")
        void success() throws Exception {
            Asset asset = assetRepository.save(TestFixture.createAsset(userA));

            mockMvc.perform(delete("/api/assets/{id}", asset.getId())
                            .cookie(authCookie(userA)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("인증 없이 접근 → 401")
        void unauthorized() throws Exception {
            Asset asset = assetRepository.save(TestFixture.createAsset(userA));

            mockMvc.perform(delete("/api/assets/{id}", asset.getId()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("다른 유저의 자산 삭제 → 403")
        void forbidden() throws Exception {
            Asset asset = assetRepository.save(TestFixture.createAsset(userA));

            mockMvc.perform(delete("/api/assets/{id}", asset.getId())
                            .cookie(authCookie(userB)))
                    .andExpect(status().isForbidden());
        }
    }

    // ─── GET /api/assets/linked ──────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/assets/linked")
    class GetLinkedAssets {

        @Test
        @DisplayName("정상 투자 연동 자산 조회 → 200")
        void success() throws Exception {
            assetRepository.save(TestFixture.createLinkedAsset(userA));
            assetRepository.save(TestFixture.createAsset(userA));   // 비연동

            mockMvc.perform(get("/api/assets/linked")
                            .cookie(authCookie(userA)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(1));
        }

        @Test
        @DisplayName("인증 없이 접근 → 401")
        void unauthorized() throws Exception {
            mockMvc.perform(get("/api/assets/linked"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─── PATCH /api/assets/{id}/reorder ─────────────────────────────────────────

    @Nested
    @DisplayName("PATCH /api/assets/{id}/reorder")
    class ReorderAsset {

        @Test
        @DisplayName("정상 순서 변경 → 200")
        void success() throws Exception {
            Asset asset1 = assetRepository.save(TestFixture.createAsset(userA, 1));
            assetRepository.save(TestFixture.createAsset(userA, 2));
            String body = objectMapper.writeValueAsString(Map.of("targetPosition", 2));

            mockMvc.perform(patch("/api/assets/{id}/reorder", asset1.getId())
                            .cookie(authCookie(userA))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("인증 없이 접근 → 401")
        void unauthorized() throws Exception {
            Asset asset = assetRepository.save(TestFixture.createAsset(userA));
            String body = objectMapper.writeValueAsString(Map.of("targetPosition", 1));

            mockMvc.perform(patch("/api/assets/{id}/reorder", asset.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─── GET /api/assets/dashboard/summary ──────────────────────────────────────

    @Nested
    @DisplayName("GET /api/assets/dashboard/summary")
    class GetDashboardSummary {

        @Test
        @DisplayName("정상 대시보드 요약 조회 → 200")
        void success() throws Exception {
            mockMvc.perform(get("/api/assets/dashboard/summary")
                            .cookie(authCookie(userA)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalAmount").exists())
                    .andExpect(jsonPath("$.data.totalMonthlyPayment").exists());
        }

        @Test
        @DisplayName("인증 없이 접근 → 401")
        void unauthorized() throws Exception {
            mockMvc.perform(get("/api/assets/dashboard/summary"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─── GET /api/assets/dashboard/chart ────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/assets/dashboard/chart")
    class GetDashboardChart {

        @Test
        @DisplayName("정상 차트 데이터 조회 → 200")
        void success() throws Exception {
            mockMvc.perform(get("/api/assets/dashboard/chart")
                            .cookie(authCookie(userA)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.items").isArray());
        }

        @Test
        @DisplayName("인증 없이 접근 → 401")
        void unauthorized() throws Exception {
            mockMvc.perform(get("/api/assets/dashboard/chart"))
                    .andExpect(status().isUnauthorized());
        }
    }
}