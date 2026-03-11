package com.asset.asset_backend.domains.asset.service;

import com.asset.asset_backend.common.enums.AssetType;
import com.asset.asset_backend.common.exception.BaseException;
import com.asset.asset_backend.common.exception.ErrorCode;
import com.asset.asset_backend.domains.asset.dto.request.AssetCreateRequest;
import com.asset.asset_backend.domains.asset.dto.request.AssetReorderRequest;
import com.asset.asset_backend.domains.asset.dto.request.AssetUpdateRequest;
import com.asset.asset_backend.domains.asset.dto.response.AssetSummaryResponse;
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
        // 새 항목은 맨 뒤에 추가
        Integer maxSortOrder = assetRepository.findMaxSortOrder();
        Asset asset = Asset.createAsset(
                request.getCategory(),
                request.getOwner(),
                request.getAmount(),
                request.getType(),
                request.getMonthlyPayment(),
                request.getPaymentDay(),
                request.getNote(),
                request.getLinkedToInvestment(),
                maxSortOrder + 1   // ✅ 맨 뒤 순서로 생성
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

    @Transactional
    public void reorderAsset(Long id, AssetReorderRequest request) {
        Asset asset = getAssetById(id);
        Integer currentPosition = asset.getSortOrder();
        Integer targetPosition = request.getTargetPosition();

        if (currentPosition.equals(targetPosition)) return;

        if (currentPosition > targetPosition) {
            // 위로 이동: target ~ current-1 사이 항목들 +1
            assetRepository.incrementSortOrderBetween(targetPosition, currentPosition);
        } else {
            // 아래로 이동: current+1 ~ target 사이 항목들 -1
            assetRepository.decrementSortOrderBetween(currentPosition, targetPosition);
        }

        asset.updateSortOrder(targetPosition);
    }

    public AssetSummaryResponse getSummary() {
        Long totalAmount = assetRepository.sumAllAmount();
        Long totalMonthlyPayment = assetRepository.sumAllMonthlyPayment();
        return AssetSummaryResponse.from(totalAmount, totalMonthlyPayment);
    }
}