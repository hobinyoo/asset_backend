package com.asset.asset_backend.domains.investment.service;

import org.springframework.cache.annotation.Cacheable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private static final String URL = "https://api.frankfurter.app/latest?from=USD&to=KRW";

    private final RestTemplate restTemplate;

    @Cacheable(value = "exchangeRate", key = "'USD_KRW'")
    public Double getUsdToKrw() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(URL, HttpMethod.GET, entity, Map.class);

            Map<String, Object> rates = (Map<String, Object>) response.getBody().get("rates");
            Number krw = (Number) rates.get("KRW");
            log.info("환율 조회 성공: USD/KRW = {}", krw.doubleValue());
            return krw.doubleValue();

        } catch (Exception e) {
            log.warn("환율 조회 실패: {}", e.getMessage());
            return null;
        }
    }
}
