package com.asset.asset_backend.domains.news.dto.news;

public record CollectResult(
        String ticker,
        String stockName,
        int collected,
        int skipped
) {}