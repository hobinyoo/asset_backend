package com.asset.asset_backend.domains.news.dto.news;

public record EmbedProgress(
        String title,
        String summary,
        int current,
        int total
) {}