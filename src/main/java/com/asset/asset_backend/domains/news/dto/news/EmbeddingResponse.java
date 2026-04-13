package com.asset.asset_backend.domains.news.dto.news;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class EmbeddingResponse {

    private String object;
    private List<EmbeddingData> data;
    private String model;

    public List<Float> getFirstEmbedding() {
        if (data == null || data.isEmpty()) {
            throw new IllegalStateException("임베딩 결과가 없습니다.");
        }
        return data.get(0).getEmbedding();
    }

    @Getter
    @NoArgsConstructor
    public static class EmbeddingData {
        private String object;
        private List<Float> embedding;
        private int index;
    }
}
