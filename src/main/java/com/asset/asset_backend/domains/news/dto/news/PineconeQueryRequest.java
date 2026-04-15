package com.asset.asset_backend.domains.news.dto.news;

import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class PineconeQueryRequest {

    private final List<Float> vector;
    private final int topK;
    private final boolean includeMetadata;
    private final String namespace;
    private final Map<String, Object> filter;

    private PineconeQueryRequest(List<Float> vector, int topK, boolean includeMetadata, String namespace, Map<String, Object> filter) {
        this.vector = vector;
        this.topK = topK;
        this.includeMetadata = includeMetadata;
        this.namespace = namespace;
        this.filter = filter;
    }

    public static PineconeQueryRequest of(List<Float> vector, int topK, String namespace) {
        return new PineconeQueryRequest(vector, topK, true, namespace, null);
    }

    public static PineconeQueryRequest of(List<Float> vector, int topK, String namespace, Map<String, Object> filter) {
        return new PineconeQueryRequest(vector, topK, true, namespace, filter);
    }
}
