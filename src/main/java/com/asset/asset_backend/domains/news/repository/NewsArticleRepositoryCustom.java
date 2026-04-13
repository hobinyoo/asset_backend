package com.asset.asset_backend.domains.news.repository;

import com.asset.asset_backend.domains.news.entity.NewsArticle;

import java.time.LocalDateTime;
import java.util.List;

public interface NewsArticleRepositoryCustom {

    List<NewsArticle> findAllNotEmbedded();

    List<NewsArticle> findByDateRange(LocalDateTime start, LocalDateTime end);
}
