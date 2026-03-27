package com.asset.asset_backend.domains.investment.controller;

import com.asset.asset_backend.common.response.ApiResult;
import com.asset.asset_backend.domains.investment.dto.response.InvestmentDashboardChartResponse;
import com.asset.asset_backend.domains.investment.dto.response.InvestmentDashboardSummaryResponse;
import com.asset.asset_backend.domains.investment.service.InvestmentDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/investments/dashboard")
@RequiredArgsConstructor
public class InvestmentDashboardController {

    private final InvestmentDashboardService investmentDashboardService;

    /**
     * 투자 대시보드 요약
     * GET /api/investments/dashboard/summary
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResult<InvestmentDashboardSummaryResponse>> getSummary(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(
                ApiResult.success(investmentDashboardService.getSummary(userId), "투자 대시보드 요약을 조회했습니다.")
        );
    }

    /**
     * 투자 대시보드 차트 데이터
     * GET /api/investments/dashboard/chart?period=7d|30d|90d|1y
     */
    @GetMapping("/chart")
    public ResponseEntity<ApiResult<InvestmentDashboardChartResponse>> getChart(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "30d") String period) {
        return ResponseEntity.ok(
                ApiResult.success(investmentDashboardService.getChart(userId, period), "투자 대시보드 차트 데이터를 조회했습니다.")
        );
    }
}