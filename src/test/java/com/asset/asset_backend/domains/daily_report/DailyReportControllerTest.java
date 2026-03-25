package com.asset.asset_backend.domains.daily_report;

import com.asset.asset_backend.BaseControllerTest;
import com.asset.asset_backend.common.fixture.TestFixture;
import com.asset.asset_backend.domains.asset.entity.Asset;
import com.asset.asset_backend.domains.auth.entity.Member;
import com.asset.asset_backend.domains.daily_report.entity.DailyReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("DailyReportController 통합 테스트")
class DailyReportControllerTest extends BaseControllerTest {

    private Member memberA;
    private Member memberB;

    @BeforeEach
    void setUp() {
        memberA = memberRepository.save(
                TestFixture.createMember(TestFixture.MEMBER_A_ID, passwordEncoder.encode(TestFixture.RAW_PASSWORD)));
        memberB = memberRepository.save(
                TestFixture.createMember(TestFixture.MEMBER_B_ID, passwordEncoder.encode(TestFixture.RAW_PASSWORD)));

        // 외부 API mock 설정
        when(claudeApiService.generateReport(anyString())).thenReturn("<p>테스트 리포트 내용</p>");
    }

    // ─── POST /api/reports/generate ──────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/reports/generate")
    class GenerateReport {

        @Test
        @DisplayName("투자 종목이 있을 때 리포트 생성 → 200")
        void success() throws Exception {
            // given
            Asset assetA = assetRepository.save(TestFixture.createLinkedAsset(memberA));
            investmentRepository.save(TestFixture.createInvestment(assetA));

            // when & then
            mockMvc.perform(post("/api/reports/generate")
                            .cookie(authCookie(TestFixture.MEMBER_A_ID)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.fullContent").exists());
        }

        @Test
        @DisplayName("이미 오늘 리포트가 존재하면 기존 리포트 반환 → 200")
        void returnsExistingReport() throws Exception {
            // given
            DailyReport existing = dailyReportRepository.save(
                    DailyReport.create(LocalDate.now(), "<p>기존 리포트</p>", "요약", memberA));

            // when & then
            mockMvc.perform(post("/api/reports/generate")
                            .cookie(authCookie(TestFixture.MEMBER_A_ID)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(existing.getId()));
        }

        @Test
        @DisplayName("인증 없이 접근 → 401")
        void unauthorized() throws Exception {
            mockMvc.perform(post("/api/reports/generate"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─── GET /api/reports ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/reports")
    class GetAllReports {

        @Test
        @DisplayName("정상 목록 조회 → 200, 본인 데이터만")
        void success() throws Exception {
            // given
            dailyReportRepository.save(DailyReport.create(LocalDate.now(), "<p>A 리포트</p>", "A 요약", memberA));
            dailyReportRepository.save(DailyReport.create(LocalDate.now().minusDays(1), "<p>B 리포트</p>", "B 요약", memberB));

            // when & then
            mockMvc.perform(get("/api/reports")
                            .cookie(authCookie(TestFixture.MEMBER_A_ID)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(1));
        }

        @Test
        @DisplayName("리포트 없으면 빈 목록 → 200")
        void emptyList() throws Exception {
            mockMvc.perform(get("/api/reports")
                            .cookie(authCookie(TestFixture.MEMBER_A_ID)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("인증 없이 접근 → 401")
        void unauthorized() throws Exception {
            mockMvc.perform(get("/api/reports"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─── GET /api/reports/{date} ─────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/reports/{date}")
    class GetReportByDate {

        @Test
        @DisplayName("정상 날짜별 조회 → 200")
        void success() throws Exception {
            // given
            LocalDate today = LocalDate.now();
            dailyReportRepository.save(DailyReport.create(today, "<p>리포트</p>", "요약", memberA));

            // when & then
            mockMvc.perform(get("/api/reports/{date}", today)
                            .cookie(authCookie(TestFixture.MEMBER_A_ID)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.reportDate").value(today.toString()));
        }

        @Test
        @DisplayName("존재하지 않는 날짜 → 500")
        void notFound() throws Exception {
            mockMvc.perform(get("/api/reports/{date}", LocalDate.now().minusDays(100))
                            .cookie(authCookie(TestFixture.MEMBER_A_ID)))
                    .andExpect(status().is5xxServerError());
        }

        @Test
        @DisplayName("인증 없이 접근 → 401")
        void unauthorized() throws Exception {
            mockMvc.perform(get("/api/reports/{date}", LocalDate.now()))
                    .andExpect(status().isUnauthorized());
        }
    }
}
