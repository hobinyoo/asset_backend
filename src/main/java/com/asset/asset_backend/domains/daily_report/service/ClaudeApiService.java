package com.asset.asset_backend.domains.daily_report.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClaudeApiService {

    @Value("${claude.api.key}")
    private String apiKey;

    private static final String CLAUDE_API_URL = "https://api.anthropic.com/v1/messages";
    private static final String MODEL = "claude-sonnet-4-6";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public String generateReport(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", apiKey);
            headers.set("anthropic-version", "2023-06-01");

            Map<String, Object> body = Map.of(
                    "model", MODEL,
                    "max_tokens", 4000,
                    "messages", List.of(
                            Map.of("role", "user", "content", prompt)
                    ),
                    "tools", List.of(Map.of("type", "web_search_20250305", "name", "web_search"))
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    CLAUDE_API_URL, HttpMethod.POST, request, Map.class
            );

            List<Map<String, Object>> content = (List<Map<String, Object>>) response.getBody().get("content");
            return content.stream()
                    .filter(c -> "text".equals(c.get("type")))
                    .map(c -> (String) c.get("text"))
                    .collect(Collectors.joining());


        } catch (Exception e) {
            log.error("Claude API 호출 실패: {}", e.getMessage());
            throw new RuntimeException("Claude API 호출 실패", e);
        }
    }
}