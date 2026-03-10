package com.asset.asset_backend.domains.investment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockPriceService {

    private final RestTemplate restTemplate;

    private static final String YAHOO_URL =
            "https://query1.finance.yahoo.com/v8/finance/chart/%s?interval=1d&range=1d";

    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    public Long getCurrentPrice(String ticker) {
        if (ticker == null || ticker.isBlank()) return null;

        try {
            String url = String.format(YAHOO_URL, ticker);

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", USER_AGENT);
            headers.set("Accept", "application/json");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            Map chart = (Map) response.getBody().get("chart");
            List result = (List) chart.get("result");

            if (result == null || result.isEmpty()) return null;

            Map meta = (Map) ((Map) result.get(0)).get("meta");
            Object price = meta.get("regularMarketPrice");

            if (price == null) return null;

            return ((Number) price).longValue();

        } catch (Exception e) {
            log.warn("현재가 조회 실패 - ticker: {}, 이유: {}", ticker, e.getMessage());
            return null;
        }
    }
}