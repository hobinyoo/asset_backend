package com.asset.asset_backend.domains.news.repository;

import com.asset.asset_backend.domains.news.entity.NewsArticle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NewsArticleRepository extends JpaRepository<NewsArticle, Long>, NewsArticleRepositoryCustom {

    Optional<NewsArticle> findByUrl(String url);

    boolean existsByUrl(String url);
}
