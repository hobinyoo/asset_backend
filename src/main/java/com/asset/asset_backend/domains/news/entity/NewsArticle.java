package com.asset.asset_backend.domains.news.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "news_articles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NewsArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(length = 1000)
    private String url;

    @Column(length = 100)
    private String source;

    private LocalDateTime publishedAt;

    @Column(length = 20)
    private String ticker;

    @Column(nullable = false)
    private Boolean embedded = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static NewsArticle create(String title, String content, String url,
                                     String source, LocalDateTime publishedAt, String ticker) {
        NewsArticle article = new NewsArticle();
        article.title = title;
        article.content = content;
        article.url = url;
        article.source = source;
        article.publishedAt = publishedAt;
        article.ticker = ticker;
        article.embedded = false;
        return article;
    }

    public void markEmbedded() {
        this.embedded = true;
    }
}
