package com.asset.asset_backend.domains.asset.repository;


import com.asset.asset_backend.common.enums.AssetType;
import com.asset.asset_backend.domains.asset.entity.Asset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AssetRepositoryCustom {
    Page<Asset> searchAssets(String category, String owner, AssetType type, Pageable pageable);
}