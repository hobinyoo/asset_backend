package com.asset.asset_backend.domains.asset.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AssetReorderRequest {
    private Integer targetPosition; // 이동할 위치 (1-based)
}