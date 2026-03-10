package com.asset.asset_backend.domains.asset.service;

import com.asset.asset_backend.common.enums.AssetType;
import com.asset.asset_backend.common.exception.BaseException;
import com.asset.asset_backend.common.exception.ErrorCode;
import com.asset.asset_backend.domains.asset.dto.request.AssetCreateRequest;
import com.asset.asset_backend.domains.asset.dto.request.AssetUpdateRequest;
import com.asset.asset_backend.domains.asset.entity.Asset;

import com.asset.asset_backend.domains.asset.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssetService {

    private final AssetRepository assetRepository;

    @Transactional
    public Asset createAsset(AssetCreateRequest request) {
        Asset asset = Asset.createAsset(
                request.getCategory(),
                request.getOwner(),
                request.getAmount(),
                request.getType(),
                request.getMonthlyPayment(),
                request.getPaymentDay(),
                request.getNote(),
                request.getLinkedToInvestment()
        );
        return assetRepository.save(asset);
    }

    public Page<Asset> searchAssets(String category, String owner, AssetType type, Pageable pageable) {
        return assetRepository.searchAssets(category, owner, type, pageable);
    }

    public Asset getAssetById(Long id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Asset ID: " + id));
    }

    @Transactional
    public Asset updateAsset(Long id, AssetUpdateRequest request) {
        Asset asset = getAssetById(id);
        asset.updateAssetInfo(
                request.getCategory(),
                request.getOwner(),
                request.getAmount(),
                request.getType(),
                request.getMonthlyPayment(),
                request.getPaymentDay(),
                request.getNote(),
                request.getLinkedToInvestment()
        );
        return asset; // @Transactional 이라 save() 불필요
    }

    @Transactional
    public void deleteAsset(Long id) {
        Asset asset = getAssetById(id);
        assetRepository.delete(asset);
    }

    @Transactional
    public List<Asset> getLinkedAssets() {
        return assetRepository.findByLinkedToInvestmentTrue();
    }
}