package com.asset.asset_backend.domains.news.service;

import com.asset.asset_backend.common.enums.MarketType;
import com.asset.asset_backend.domains.investment.entity.Investment;
import com.asset.asset_backend.domains.investment.repository.InvestmentRepository;
import com.asset.asset_backend.domains.news.dto.news.PineconeUpsertRequest;
import com.asset.asset_backend.domains.news.entity.NewsArticle;
import com.asset.asset_backend.domains.news.repository.NewsArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.jsoup.Jsoup;
import org.springframework.web.util.UriComponentsBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NewsCollectorService {

    @Value("${news.naver.client-id}")
    private String naverClientId;

    @Value("${news.naver.client-secret}")
    private String naverClientSecret;

    @Value("${news.google-rss.base-url}")
    private String googleRssBaseUrl;

    private static final String NAVER_NEWS_URL = "https://openapi.naver.com/v1/search/news";
    private static final int DATE_FILTER_DAYS = 7;

    private final NewsArticleRepository newsArticleRepository;
    private final InvestmentRepository investmentRepository;
    private final EmbeddingService embeddingService;
    private final PineconeService pineconeService;
    private final RestTemplate restTemplate;

    @Transactional
    public int collect(Long userId) {
        List<Investment> investments = investmentRepository.findByAsset_UserIdWithAsset(userId);
        log.info("[NewsCollector] 뉴스 수집 시작 - userId: {}, 보유 종목 수: {}", userId, investments.size());

        int total = 0;
        for (Investment inv : investments) {
            String ticker = inv.getTicker();
            String stockName = inv.getStockName();
            if (ticker == null || ticker.isBlank()) continue;

            log.info("[NewsCollector] 종목 수집 시작 - stockName: {}, ticker: {}, MarketType: {}",
                    stockName, ticker, inv.getMarketType());
            try {
                int count;
                if (inv.getMarketType() == MarketType.DOMESTIC) {
                    count = collectFromNaver(stockName, ticker);
                } else {
                    count = collectFromGoogleRss(stockName, ticker);
                }
                log.info("[NewsCollector] 종목 수집 완료 - stockName: {}, 저장 건수: {}", stockName, count);
                total += count;
            } catch (Exception e) {
                log.error("[NewsCollector] 수집 실패 - stockName: {}, 원인: {}", stockName, e.getMessage());
            }
        }

        log.info("[NewsCollector] 뉴스 수집 완료 - 전체 저장 건수: {}", total);
        return total;
    }

    @Transactional
    public int collectFromNaver(String query, String ticker) {
        log.info("[NewsCollector] 네이버 API 호출 - query: {}", query);
        String encodeQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        URI uri = URI.create(NAVER_NEWS_URL + "?query=" + encodeQuery + "&display=10&sort=sim");
        log.info("[NewsCollector] 네이버 API URL: {}", uri);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", naverClientId);
        headers.set("X-Naver-Client-Secret", naverClientSecret);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                uri, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {}
        );

        log.info("[NewsCollector] 네이버 응답 body: {}", response.getBody());
        List<Map<String, Object>> items =
                (List<Map<String, Object>>) response.getBody().get("items");

        if (items == null) return 0;
        log.info("[NewsCollector] 네이버 응답 - {}건", items.size());

        LocalDateTime cutoff = LocalDateTime.now().minusDays(DATE_FILTER_DAYS);
        int saved = 0;
        for (Map<String, Object> item : items) {
            String articleUrl = (String) item.get("link");
            if (newsArticleRepository.existsByUrl(articleUrl)) {
                log.info("[NewsCollector] 중복 URL 스킵 - {}", articleUrl);
                continue;
            }

            LocalDateTime publishedAt = parsePublishedAt((String) item.get("pubDate"));
            if (publishedAt != null && publishedAt.isBefore(cutoff)) {
                log.info("[NewsCollector] 날짜 필터 스킵 - pubDate: {}", item.get("pubDate"));
                continue;
            }

            String title = stripHtmlTags((String) item.get("title"));
            String originalLink = (String) item.get("originallink");
            String description = stripHtmlTags((String) item.get("description"));

            String content = null;
            if (originalLink != null && !originalLink.isBlank()) {
                content = crawlContent(originalLink);
            }
            if (content != null && !content.isBlank()) {
                log.info("[NewsCollector] 크롤링 성공 - url: {}, contentLength: {}", originalLink, content.length());
            } else {
                log.warn("[NewsCollector] 크롤링 실패, description fallback - url: {}", originalLink);
                content = description;
            }

            NewsArticle article = NewsArticle.create(title, content, articleUrl,
                    "naver", publishedAt, ticker);
            newsArticleRepository.save(article);
            log.info("[NewsCollector] 저장 완료 - title: {}", title);
            saved++;
        }
        return saved;
    }

    @Transactional
    public int collectFromGoogleRss(String query, String ticker) {
        log.info("[NewsCollector] Google RSS 호출 - query: {}", query);
        String url = UriComponentsBuilder.fromHttpUrl(googleRssBaseUrl)
                .queryParam("q", query)
                .queryParam("hl", "en")
                .queryParam("gl", "US")
                .queryParam("ceid", "US:en")
                .toUriString();

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        String xml = response.getBody();
        if (xml == null) return 0;

        int saved = 0;
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            NodeList items = doc.getElementsByTagName("item");
            log.info("[NewsCollector] Google RSS 응답 - {}건", items.getLength());

            LocalDateTime cutoff = LocalDateTime.now().minusDays(DATE_FILTER_DAYS);
            for (int i = 0; i < items.getLength(); i++) {
                Element item = (Element) items.item(i);
                String articleUrl = getTagText(item, "link");
                if (articleUrl == null || newsArticleRepository.existsByUrl(articleUrl)) {
                    log.info("[NewsCollector] 중복 URL 스킵 - {}", articleUrl);
                    continue;
                }

                String pubDateStr = getTagText(item, "pubDate");
                LocalDateTime publishedAt = parsePublishedAt(pubDateStr);
                if (publishedAt != null && publishedAt.isBefore(cutoff)) {
                    log.info("[NewsCollector] 날짜 필터 스킵 - pubDate: {}", pubDateStr);
                    continue;
                }

                String title = getTagText(item, "title");
                String content = getTagText(item, "description");

                NewsArticle article = NewsArticle.create(title, content, articleUrl,
                        "google_rss", publishedAt, ticker);
                newsArticleRepository.save(article);
                log.info("[NewsCollector] 저장 완료 - title: {}", title);
                saved++;
            }
        } catch (Exception e) {
            log.error("[NewsCollector] Google RSS 파싱 실패 query={}: {}", query, e.getMessage());
        }
        return saved;
    }

    @Transactional
    public int embedAll() {
        List<NewsArticle> unembedded = newsArticleRepository.findAllNotEmbedded();
        if (unembedded.isEmpty()) {
            log.info("[NewsCollector] 임베딩할 기사 없음");
            return 0;
        }

        log.info("[NewsCollector] 임베딩 처리 시작 - {}건", unembedded.size());
        List<PineconeUpsertRequest.Vector> vectors = new ArrayList<>();

        for (NewsArticle article : unembedded) {
            try {
                String text = article.getTitle() + " " +
                        (article.getContent() != null ? article.getContent() : "");
                List<Float> embedding = embeddingService.embed(text);

                Map<String, Object> metadata = buildMetadata(article);
                PineconeUpsertRequest.Vector vector = PineconeUpsertRequest.Vector.of(
                        "news_" + article.getId(), embedding, metadata
                );
                vectors.add(vector);
                article.markEmbedded();

            } catch (Exception e) {
                log.error("[NewsCollector] 기사 임베딩 실패 id={}: {}", article.getId(), e.getMessage());
            }
        }

        if (!vectors.isEmpty()) {
            pineconeService.upsert(vectors);
            log.info("[NewsCollector] Pinecone upsert 완료 - {}건", vectors.size());
        }
        return vectors.size();
    }

    private Map<String, Object> buildMetadata(NewsArticle article) {
        Map<String, Object> metadata = new java.util.HashMap<>();
        metadata.put("title", article.getTitle() != null ? article.getTitle() : "");
        metadata.put("url", article.getUrl() != null ? article.getUrl() : "");
        metadata.put("source", article.getSource() != null ? article.getSource() : "");
        metadata.put("ticker", article.getTicker() != null ? article.getTicker() : "");
        metadata.put("publishedAt", article.getPublishedAt() != null
                ? article.getPublishedAt().toEpochSecond(ZoneOffset.UTC) : 0L);
        return metadata;
    }

    private LocalDateTime parsePublishedAt(String pubDate) {
        if (pubDate == null || pubDate.isBlank()) return null;
        try {
            return ZonedDateTime.parse(pubDate.trim(), DateTimeFormatter.RFC_1123_DATE_TIME)
                    .toLocalDateTime();
        } catch (Exception e) {
            log.warn("[NewsCollector] pubDate 파싱 실패 - {}: {}", pubDate, e.getMessage());
            return null;
        }
    }

    private String getTagText(Element element, String tagName) {
        NodeList nodes = element.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) return null;
        return nodes.item(0).getTextContent();
    }

    private String crawlContent(String url) {
        try {
            org.jsoup.nodes.Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(5000)
                    .get();
            String[] selectors = {"#article-view-content-div", ".article-body", "article"};
            for (String selector : selectors) {
                String text = doc.select(selector).text();
                if (text != null && !text.isBlank()) {
                    return text;
                }
            }
            return null;
        } catch (Exception e) {
            log.warn("[NewsCollector] 크롤링 예외 - url: {}, 원인: {}", url, e.getMessage());
            return null;
        }
    }

    private String stripHtmlTags(String html) {
        if (html == null) return null;
        return html.replaceAll("<[^>]*>", "").trim();
    }
}