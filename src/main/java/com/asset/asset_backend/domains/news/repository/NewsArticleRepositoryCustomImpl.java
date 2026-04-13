package com.asset.asset_backend.domains.news.repository;

import com.asset.asset_backend.domains.news.entity.NewsArticle;
import com.asset.asset_backend.domains.news.entity.QNewsArticle;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class NewsArticleRepositoryCustomImpl implements NewsArticleRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<NewsArticle> findAllNotEmbedded() {
        QNewsArticle newsArticle = QNewsArticle.newsArticle;

        return queryFactory
                .selectFrom(newsArticle)
                .where(newsArticle.embedded.isFalse())
                .orderBy(newsArticle.createdAt.asc())
                .fetch();
    }

    @Override
    public List<NewsArticle> findByDateRange(LocalDateTime start, LocalDateTime end) {
        QNewsArticle newsArticle = QNewsArticle.newsArticle;

        return queryFactory
                .selectFrom(newsArticle)
                .where(newsArticle.publishedAt.between(start, end))
                .orderBy(newsArticle.publishedAt.desc())
                .fetch();
    }
}
