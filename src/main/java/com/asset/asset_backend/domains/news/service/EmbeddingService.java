package com.asset.asset_backend.domains.news.service;

import com.asset.asset_backend.domains.news.dto.news.EmbeddingRequest;
import com.asset.asset_backend.domains.news.dto.news.EmbeddingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.embedding-url}")
    private String embeddingUrl;

    private final RestTemplate restTemplate;

    public List<Float> embed(String text) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        EmbeddingRequest request = EmbeddingRequest.of(text);
        HttpEntity<EmbeddingRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<EmbeddingResponse> response = restTemplate.exchange(
                embeddingUrl, HttpMethod.POST, entity, EmbeddingResponse.class
        );

        return response.getBody().getFirstEmbedding();
    }
}