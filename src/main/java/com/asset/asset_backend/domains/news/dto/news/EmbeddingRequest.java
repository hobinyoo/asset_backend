package com.asset.asset_backend.domains.news.dto.news;

import lombok.Getter;

@Getter
public class EmbeddingRequest {

    private final String model;
    private final String input;

    private EmbeddingRequest(String model, String input) {
        this.model = model;
        this.input = input;
    }

    public static EmbeddingRequest of(String input) {
        return new EmbeddingRequest("text-embedding-3-small", input);
    }
}
