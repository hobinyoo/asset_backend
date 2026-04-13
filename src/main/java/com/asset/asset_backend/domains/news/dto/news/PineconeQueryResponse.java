package com.asset.asset_backend.domains.news.dto.news;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
public class PineconeQueryResponse {

    private List<Match> matches;
    private String namespace;

    @Getter
    @NoArgsConstructor
    public static class Match {
        private String id;
        private float score;
        private Map<String, String> metadata;
    }
}
