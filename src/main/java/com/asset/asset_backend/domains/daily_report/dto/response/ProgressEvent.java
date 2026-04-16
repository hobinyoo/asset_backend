package com.asset.asset_backend.domains.daily_report.dto.response;

public record ProgressEvent(
        String type,
        String status,
        String ticker,
        String stockName,
        String message,
        int current,
        int total,
        Object data
) {}
