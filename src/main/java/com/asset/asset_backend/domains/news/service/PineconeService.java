package com.asset.asset_backend.domains.news.service;

import com.asset.asset_backend.domains.news.dto.news.PineconeQueryRequest;
import com.asset.asset_backend.domains.news.dto.news.PineconeQueryResponse;
import com.asset.asset_backend.domains.news.dto.news.PineconeUpsertRequest;
import com.asset.asset_backend.domains.news.dto.news.PineconeUpsertResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PineconeService {

    @Value("${pinecone.api-key}")
    private String apiKey;

    @Value("${pinecone.host}")
    private String pineconeHost;

    @Value("${pinecone.index-name}")
    private String indexName;

    private static final String NAMESPACE = "news";

    private final RestTemplate restTemplate;
    private final EmbeddingService embeddingService;

    public PineconeUpsertResponse upsert(List<PineconeUpsertRequest.Vector> vectors) {
        HttpHeaders headers = buildHeaders();
        PineconeUpsertRequest request = PineconeUpsertRequest.of(vectors, NAMESPACE);
        HttpEntity<PineconeUpsertRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<PineconeUpsertResponse> response = restTemplate.exchange(
                pineconeHost + "/vectors/upsert", HttpMethod.POST, entity, PineconeUpsertResponse.class
        );

        log.info("[Pinecone] upserted {} vectors", response.getBody().getUpsertedCount());
        return response.getBody();
    }

    public PineconeQueryResponse query(String text, int topK) {
        List<Float> vector = embeddingService.embed(text);
        return query(vector, topK, null);
    }

    public PineconeQueryResponse query(String text, int topK, Map<String, Object> filter) {
        List<Float> vector = embeddingService.embed(text);
        return query(vector, topK, filter);
    }

    public PineconeQueryResponse query(List<Float> vector, int topK) {
        return query(vector, topK, null);
    }

    public PineconeQueryResponse query(List<Float> vector, int topK, Map<String, Object> filter) {
        HttpHeaders headers = buildHeaders();
        PineconeQueryRequest request = PineconeQueryRequest.of(vector, topK, NAMESPACE, filter);
        HttpEntity<PineconeQueryRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<PineconeQueryResponse> response = restTemplate.exchange(
                pineconeHost + "/query", HttpMethod.POST, entity, PineconeQueryResponse.class
        );

        return response.getBody();
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Api-Key", apiKey);
        return headers;
    }
}
