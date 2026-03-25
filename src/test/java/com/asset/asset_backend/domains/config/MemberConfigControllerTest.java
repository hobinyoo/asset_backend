package com.asset.asset_backend.domains.config;

import com.asset.asset_backend.BaseControllerTest;
import com.asset.asset_backend.common.fixture.TestFixture;
import com.asset.asset_backend.domains.auth.entity.Member;
import com.asset.asset_backend.domains.config.entity.ConfigType;
import com.asset.asset_backend.domains.config.entity.MemberConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("MemberConfigController 통합 테스트")
class MemberConfigControllerTest extends BaseControllerTest {

    private Member memberA;
    private Member memberB;

    @BeforeEach
    void setUp() {
        memberA = memberRepository.save(
                TestFixture.createMember(TestFixture.MEMBER_A_ID, passwordEncoder.encode(TestFixture.RAW_PASSWORD)));
        memberB = memberRepository.save(
                TestFixture.createMember(TestFixture.MEMBER_B_ID, passwordEncoder.encode(TestFixture.RAW_PASSWORD)));
    }

    // ─── GET /api/config/asset-categories ────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/config/asset-categories")
    class GetAssetCategories {

        @Test
        @DisplayName("커스텀 없으면 기본값만 반환 → 200")
        void success_returnsDefaults() throws Exception {
            mockMvc.perform(get("/api/config/asset-categories")
                            .cookie(authCookie(TestFixture.MEMBER_A_ID)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(7));  // DEFAULT_ASSET_CATEGORIES 7개
        }

        @Test
        @DisplayName("커스텀 값이 있으면 기본값 + 커스텀 같이 반환 → 200")
        void success_returnsDefaultsPlusCustom() throws Exception {
            // given
            memberConfigRepository.save(TestFixture.createMemberConfig(memberA, ConfigType.ASSET_CATEGORY, "펀드"));
            memberConfigRepository.save(TestFixture.createMemberConfig(memberA, ConfigType.ASSET_CATEGORY, "파킹통장"));

            // when & then - 기본값 7 + 커스텀 2 = 9
            mockMvc.perform(get("/api/config/asset-categories")
                            .cookie(authCookie(TestFixture.MEMBER_A_ID)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(9));
        }

        @Test
        @DisplayName("본인 커스텀만 포함 → 200")
        void success_onlyOwnCustom() throws Exception {
            // given
            memberConfigRepository.save(TestFixture.createMemberConfig(memberA, ConfigType.ASSET_CATEGORY, "펀드"));
            memberConfigRepository.save(TestFixture.createMemberConfig(memberB, ConfigType.ASSET_CATEGORY, "B펀드"));

            // when & then - 기본값 7 + memberA 커스텀 1 = 8
            mockMvc.perform(get("/api/config/asset-categories")
                            .cookie(authCookie(TestFixture.MEMBER_A_ID)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(8));
        }

        @Test
        @DisplayName("인증 없이 접근 → 401")
        void unauthorized() throws Exception {
            mockMvc.perform(get("/api/config/asset-categories"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─── POST /api/config/asset-categories ───────────────────────────────────────

    @Nested
    @DisplayName("POST /api/config/asset-categories")
    class AddAssetCategory {

        @Test
        @DisplayName("정상 추가 → 201")
        void success() throws Exception {
            String body = objectMapper.writeValueAsString(Map.of("value", "펀드"));

            mockMvc.perform(post("/api/config/asset-categories")
                            .cookie(authCookie(TestFixture.MEMBER_A_ID))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.value").value("펀드"));
        }

        @Test
        @DisplayName("커스텀 중복 값 추가 → 409")
        void duplicate_custom() throws Exception {
            // given
            memberConfigRepository.save(TestFixture.createMemberConfig(memberA, ConfigType.ASSET_CATEGORY, "펀드"));
            String body = objectMapper.writeValueAsString(Map.of("value", "펀드"));

            // when & then
            mockMvc.perform(post("/api/config/asset-categories")
                            .cookie(authCookie(TestFixture.MEMBER_A_ID))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("기본값과 동일한 값 추가 → 409")
        void duplicate_default() throws Exception {
            // given - "청약저축"은 기본값에 포함
            String body = objectMapper.writeValueAsString(Map.of("value", "청약저축"));

            // when & then
            mockMvc.perform(post("/api/config/asset-categories")
                            .cookie(authCookie(TestFixture.MEMBER_A_ID))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("인증 없이 접근 → 401")
        void unauthorized() throws Exception {
            mockMvc.perform(post("/api/config/asset-categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"value\":\"펀드\"}"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─── DELETE /api/config/asset-categories/{id} ────────────────────────────────

    @Nested
    @DisplayName("DELETE /api/config/asset-categories/{id}")
    class DeleteAssetCategory {

        @Test
        @DisplayName("정상 삭제 → 200")
        void success() throws Exception {
            // given
            MemberConfig config = memberConfigRepository.save(
                    TestFixture.createMemberConfig(memberA, ConfigType.ASSET_CATEGORY, "펀드"));

            // when & then
            mockMvc.perform(delete("/api/config/asset-categories/{id}", config.getId())
                            .cookie(authCookie(TestFixture.MEMBER_A_ID)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("다른 멤버의 설정 삭제 → 403")
        void forbidden() throws Exception {
            // given
            MemberConfig config = memberConfigRepository.save(
                    TestFixture.createMemberConfig(memberA, ConfigType.ASSET_CATEGORY, "펀드"));

            // when & then (memberB가 memberA 설정 삭제 시도)
            mockMvc.perform(delete("/api/config/asset-categories/{id}", config.getId())
                            .cookie(authCookie(TestFixture.MEMBER_B_ID)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("존재하지 않는 ID → 404")
        void notFound() throws Exception {
            mockMvc.perform(delete("/api/config/asset-categories/{id}", 999_999L)
                            .cookie(authCookie(TestFixture.MEMBER_A_ID)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("인증 없이 접근 → 401")
        void unauthorized() throws Exception {
            mockMvc.perform(delete("/api/config/asset-categories/{id}", 1L))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─── GET /api/config/asset-owners ────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/config/asset-owners")
    class GetAssetOwners {

        @Test
        @DisplayName("커스텀 없으면 기본값만 반환 → 200")
        void success_returnsDefaults() throws Exception {
            mockMvc.perform(get("/api/config/asset-owners")
                            .cookie(authCookie(TestFixture.MEMBER_A_ID)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(2));  // DEFAULT_ASSET_OWNERS 2개
        }

        @Test
        @DisplayName("인증 없이 접근 → 401")
        void unauthorized() throws Exception {
            mockMvc.perform(get("/api/config/asset-owners"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─── POST /api/config/asset-owners ───────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/config/asset-owners")
    class AddAssetOwner {

        @Test
        @DisplayName("정상 추가 → 201")
        void success() throws Exception {
            String body = objectMapper.writeValueAsString(Map.of("value", "배우자"));

            mockMvc.perform(post("/api/config/asset-owners")
                            .cookie(authCookie(TestFixture.MEMBER_A_ID))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.value").value("배우자"));
        }

        @Test
        @DisplayName("중복 값 추가 → 409")
        void duplicate() throws Exception {
            // given
            memberConfigRepository.save(TestFixture.createMemberConfig(memberA, ConfigType.ASSET_OWNER, "배우자"));
            String body = objectMapper.writeValueAsString(Map.of("value", "배우자"));

            // when & then
            mockMvc.perform(post("/api/config/asset-owners")
                            .cookie(authCookie(TestFixture.MEMBER_A_ID))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("인증 없이 접근 → 401")
        void unauthorized() throws Exception {
            mockMvc.perform(post("/api/config/asset-owners")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"value\":\"배우자\"}"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─── DELETE /api/config/asset-owners/{id} ────────────────────────────────────

    @Nested
    @DisplayName("DELETE /api/config/asset-owners/{id}")
    class DeleteAssetOwner {

        @Test
        @DisplayName("정상 삭제 → 200")
        void success() throws Exception {
            // given
            MemberConfig config = memberConfigRepository.save(
                    TestFixture.createMemberConfig(memberA, ConfigType.ASSET_OWNER, "배우자"));

            mockMvc.perform(delete("/api/config/asset-owners/{id}", config.getId())
                            .cookie(authCookie(TestFixture.MEMBER_A_ID)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("다른 멤버의 설정 삭제 → 403")
        void forbidden() throws Exception {
            // given
            MemberConfig config = memberConfigRepository.save(
                    TestFixture.createMemberConfig(memberA, ConfigType.ASSET_OWNER, "배우자"));

            mockMvc.perform(delete("/api/config/asset-owners/{id}", config.getId())
                            .cookie(authCookie(TestFixture.MEMBER_B_ID)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("인증 없이 접근 → 401")
        void unauthorized() throws Exception {
            mockMvc.perform(delete("/api/config/asset-owners/{id}", 1L))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─── GET /api/config/investment-categories ────────────────────────────────────

    @Nested
    @DisplayName("GET /api/config/investment-categories")
    class GetInvestmentCategories {

        @Test
        @DisplayName("커스텀 없으면 기본값만 반환 → 200")
        void success_returnsDefaults() throws Exception {
            mockMvc.perform(get("/api/config/investment-categories")
                            .cookie(authCookie(TestFixture.MEMBER_A_ID)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(5));  // DEFAULT_INVESTMENT_CATEGORIES 5개
        }

        @Test
        @DisplayName("인증 없이 접근 → 401")
        void unauthorized() throws Exception {
            mockMvc.perform(get("/api/config/investment-categories"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─── POST /api/config/investment-categories ───────────────────────────────────

    @Nested
    @DisplayName("POST /api/config/investment-categories")
    class AddInvestmentCategory {

        @Test
        @DisplayName("정상 추가 → 201")
        void success() throws Exception {
            String body = objectMapper.writeValueAsString(Map.of("value", "리츠"));

            mockMvc.perform(post("/api/config/investment-categories")
                            .cookie(authCookie(TestFixture.MEMBER_A_ID))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.value").value("리츠"));
        }

        @Test
        @DisplayName("중복 값 추가 → 409")
        void duplicate() throws Exception {
            // given
            memberConfigRepository.save(TestFixture.createMemberConfig(memberA, ConfigType.INVESTMENT_CATEGORY, "리츠"));
            String body = objectMapper.writeValueAsString(Map.of("value", "리츠"));

            // when & then
            mockMvc.perform(post("/api/config/investment-categories")
                            .cookie(authCookie(TestFixture.MEMBER_A_ID))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("인증 없이 접근 → 401")
        void unauthorized() throws Exception {
            mockMvc.perform(post("/api/config/investment-categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"value\":\"리츠\"}"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─── DELETE /api/config/investment-categories/{id} ───────────────────────────

    @Nested
    @DisplayName("DELETE /api/config/investment-categories/{id}")
    class DeleteInvestmentCategory {

        @Test
        @DisplayName("정상 삭제 → 200")
        void success() throws Exception {
            // given
            MemberConfig config = memberConfigRepository.save(
                    TestFixture.createMemberConfig(memberA, ConfigType.INVESTMENT_CATEGORY, "리츠"));

            mockMvc.perform(delete("/api/config/investment-categories/{id}", config.getId())
                            .cookie(authCookie(TestFixture.MEMBER_A_ID)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("다른 멤버의 설정 삭제 → 403")
        void forbidden() throws Exception {
            // given
            MemberConfig config = memberConfigRepository.save(
                    TestFixture.createMemberConfig(memberA, ConfigType.INVESTMENT_CATEGORY, "리츠"));

            mockMvc.perform(delete("/api/config/investment-categories/{id}", config.getId())
                            .cookie(authCookie(TestFixture.MEMBER_B_ID)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("존재하지 않는 ID → 404")
        void notFound() throws Exception {
            mockMvc.perform(delete("/api/config/investment-categories/{id}", 999_999L)
                            .cookie(authCookie(TestFixture.MEMBER_A_ID)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("인증 없이 접근 → 401")
        void unauthorized() throws Exception {
            mockMvc.perform(delete("/api/config/investment-categories/{id}", 1L))
                    .andExpect(status().isUnauthorized());
        }
    }
}
