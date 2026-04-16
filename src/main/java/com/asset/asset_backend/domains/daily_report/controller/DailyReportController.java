package com.asset.asset_backend.domains.daily_report.controller;

import com.asset.asset_backend.common.response.ApiResult;
import com.asset.asset_backend.domains.daily_report.dto.response.DailyReportResponse;
import com.asset.asset_backend.domains.daily_report.dto.response.ProgressEvent;
import com.asset.asset_backend.domains.daily_report.entity.DailyReport;
import com.asset.asset_backend.domains.daily_report.service.DailyReportService;
import com.asset.asset_backend.domains.investment.entity.Investment;
import com.asset.asset_backend.domains.news.service.NewsCollectorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class DailyReportController {

    private final DailyReportService dailyReportService;
    private final NewsCollectorService newsCollectorService;
    private final ObjectMapper objectMapper;

    // 수동으로 리포트 생성 (테스트용)
    @PostMapping("/generate")
    public ResponseEntity<ApiResult<DailyReportResponse>> generateReport(
            @AuthenticationPrincipal Long userId) {
        DailyReport report = dailyReportService.generateDailyReport(userId);
        return ResponseEntity.ok(ApiResult.success(DailyReportResponse.from(report), "리포트가 생성되었습니다."));
    }

    // SSE 기반 리포트 생성 (수집 → 임베딩 → 리포트 순서로 진행 상황 스트리밍)
    @GetMapping(value = "/generate/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter generateStream(@AuthenticationPrincipal Long userId) {
        SseEmitter emitter = new SseEmitter(5 * 60 * 1000L);

        new Thread(() -> {
            try {
                // 1. 상위 10개 종목 조회 및 COLLECT START
                List<Investment> topInvestments = newsCollectorService.getTopInvestments(userId);
                List<Map<String, String>> topStockList = topInvestments.stream()
                        .collect(Collectors.toMap(
                                inv -> inv.getTicker() != null ? inv.getTicker() : "",
                                Investment::getStockName,
                                (a, b) -> a
                        ))
                        .entrySet().stream()
                        .map(e -> Map.of("ticker", e.getKey(), "stockName", e.getValue()))
                        .collect(Collectors.toList());
                int collectTotal = topStockList.size();

                send(emitter, new ProgressEvent("COLLECT", "START", null, null,
                        "뉴스 수집 시작 - " + collectTotal + "개 종목",
                        0, collectTotal, topStockList));

                // 2. 종목별 COLLECT PROGRESS
                int[] collectIdx = {0};
                newsCollectorService.collect(userId, result -> {
                    collectIdx[0]++;
                    int fetched = result.collected() + result.skipped();
                    String msg = fetched + "건 조회 → " + result.collected() + "건 저장, " + result.skipped() + "건 스킵";
                    send(emitter, new ProgressEvent("COLLECT", "PROGRESS",
                            result.ticker(), result.stockName(), msg,
                            collectIdx[0], collectTotal, null));
                });

                // 3. COLLECT DONE
                send(emitter, new ProgressEvent("COLLECT", "DONE", null, null,
                        "수집 완료", collectTotal, collectTotal, null));

                // 4. EMBED START
                send(emitter, new ProgressEvent("EMBED", "START", null, null,
                        "임베딩 시작", 0, 0, null));

                // 5. 기사별 EMBED PROGRESS
                newsCollectorService.embedAll(progress -> send(emitter,
                        new ProgressEvent("EMBED", "PROGRESS", null, null, "요약 완료",
                                progress.current(), progress.total(),
                                Map.of("title", progress.title(), "summary", progress.summary()))));

                // 6. EMBED DONE
                send(emitter, new ProgressEvent("EMBED", "DONE", null, null,
                        "임베딩 완료", 0, 0, null));

                // 7. REPORT START
                send(emitter, new ProgressEvent("REPORT", "START", null, null,
                        "리포트 생성 중...", 0, 1, null));

                // 8. 리포트 생성
                DailyReport report = dailyReportService.generateDailyReport(userId);

                // 9. REPORT COMPLETE
                send(emitter, new ProgressEvent("REPORT", "COMPLETE", null, null,
                        "리포트 생성 완료", 1, 1, DailyReportResponse.from(report)));

                emitter.complete();

            } catch (Exception e) {
                log.error("[SSE] 리포트 생성 스트림 실패: {}", e.getMessage());
                send(emitter, new ProgressEvent("ERROR", "ERROR", null, null,
                        e.getMessage(), 0, 0, null));
                emitter.completeWithError(e);
            }
        }).start();

        return emitter;
    }

    // 전체 리포트 목록
    @GetMapping
    public ResponseEntity<ApiResult<List<DailyReportResponse>>> getAllReports(
            @AuthenticationPrincipal Long userId) {
        List<DailyReportResponse> responses = dailyReportService.getAllReports(userId).stream()
                .map(DailyReportResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResult.success(responses, "리포트 목록을 조회했습니다."));
    }

    // 날짜별 리포트 조회
    @GetMapping("/{date}")
    public ResponseEntity<ApiResult<DailyReportResponse>> getReportByDate(
            @AuthenticationPrincipal Long userId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        DailyReport report = dailyReportService.getReportByDate(date, userId);
        return ResponseEntity.ok(ApiResult.success(DailyReportResponse.from(report), "리포트를 조회했습니다."));
    }

    private void send(SseEmitter emitter, ProgressEvent event) {
        try {
            emitter.send(SseEmitter.event().data(objectMapper.writeValueAsString(event)));
        } catch (Exception e) {
            log.warn("[SSE] 이벤트 전송 실패: {}", e.getMessage());
        }
    }
}
