package com.asset.asset_backend.domains.news.dto.news;

import lombok.Getter;

import java.util.List;

@Getter
public class PineconeQueryRequest {

    private final List<Float> vector;
    private final int topK;
    private final boolean includeMetadata;
    private final String namespace;

    private PineconeQueryRequest(List<Float> vector, int topK, boolean includeMetadata, String namespace) {
        this.vector = vector;
        this.topK = topK;
        this.includeMetadata = includeMetadata;
        this.namespace = namespace;
    }

    public static PineconeQueryRequest of(List<Float> vector, int topK, String namespace) {
        return new PineconeQueryRequest(vector, topK, true, namespace);
    }
}
