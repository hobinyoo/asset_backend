package com.asset.asset_backend.domains.daily_report.dto.response;

import com.asset.asset_backend.domains.daily_report.entity.DailyReport;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class DailyReportResponse {
    private Long id;
    private LocalDate reportDate;
    private String fullContent;
    private String summaryContent;
    private String createdAt;

    public static DailyReportResponse from(DailyReport report) {
        return DailyReportResponse.builder()
                .id(report.getId())
                .reportDate(report.getReportDate())
                .fullContent(report.getFullContent())
                .summaryContent(report.getSummaryContent())
                .createdAt(report.getCreatedAt() != null ? report.getCreatedAt().toString() : null)
                .build();
    }
}