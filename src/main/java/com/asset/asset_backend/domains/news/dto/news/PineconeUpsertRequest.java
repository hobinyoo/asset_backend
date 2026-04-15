package com.asset.asset_backend.domains.news.dto.news;

import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class PineconeUpsertRequest {

    private final List<Vector> vectors;
    private final String namespace;

    private PineconeUpsertRequest(List<Vector> vectors, String namespace) {
        this.vectors = vectors;
        this.namespace = namespace;
    }

    public static PineconeUpsertRequest of(List<Vector> vectors, String namespace) {
        return new PineconeUpsertRequest(vectors, namespace);
    }

    @Getter
    public static class Vector {
        private final String id;
        private final List<Float> values;
        private final Map<String, Object> metadata;

        private Vector(String id, List<Float> values, Map<String, Object> metadata) {
            this.id = id;
            this.values = values;
            this.metadata = metadata;
        }

        public static Vector of(String id, List<Float> values, Map<String, Object> metadata) {
            return new Vector(id, values, metadata);
        }
    }
}
