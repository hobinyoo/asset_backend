package com.asset.asset_backend.domains.daily_report.controller;

import com.asset.asset_backend.common.response.ApiResult;

import com.asset.asset_backend.domains.daily_report.dto.response.DailyReportResponse;
import com.asset.asset_backend.domains.daily_report.entity.DailyReport;

import com.asset.asset_backend.domains.daily_report.service.DailyReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class DailyReportController {

    private final DailyReportService dailyReportService;

    // 수동으로 리포트 생성 (테스트용)
    @PostMapping("/generate")
    public ResponseEntity<ApiResult<DailyReportResponse>> generateReport() {
        DailyReport report = dailyReportService.generateDailyReport();
        return ResponseEntity.ok(ApiResult.success(DailyReportResponse.from(report), "리포트가 생성되었습니다."));
    }

    // 전체 리포트 목록
    @GetMapping
    public ResponseEntity<ApiResult<List<DailyReportResponse>>> getAllReports() {
        List<DailyReportResponse> responses = dailyReportService.getAllReports().stream()
                .map(DailyReportResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResult.success(responses, "리포트 목록을 조회했습니다."));
    }

    // 날짜별 리포트 조회
    @GetMapping("/{date}")
    public ResponseEntity<ApiResult<DailyReportResponse>> getReportByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        DailyReport report = dailyReportService.getReportByDate(date);
        return ResponseEntity.ok(ApiResult.success(DailyReportResponse.from(report), "리포트를 조회했습니다."));
    }
}