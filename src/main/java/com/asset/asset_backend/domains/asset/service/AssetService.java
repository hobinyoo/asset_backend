package com.asset.asset_backend.domains.asset.service;

import com.asset.asset_backend.common.enums.AssetType;
import com.asset.asset_backend.common.exception.BaseException;
import com.asset.asset_backend.common.exception.ErrorCode;
import com.asset.asset_backend.domains.asset.dto.request.AssetCreateRequest;
import com.asset.asset_backend.domains.asset.dto.request.AssetReorderRequest;
import com.asset.asset_backend.domains.asset.dto.request.AssetUpdateRequest;
import com.asset.asset_backend.domains.asset.dto.response.DashboardChartResponse;
import com.asset.asset_backend.domains.asset.dto.response.DashboardSummaryResponse;
import com.asset.asset_backend.domains.asset.entity.Asset;
import com.asset.asset_backend.domains.asset.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssetService {

    private final AssetRepository assetRepository;

    @Transactional
    public Asset createAsset(AssetCreateRequest request) {
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
                maxSortOrder + 1
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
        return asset;
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
            assetRepository.incrementSortOrderBetween(targetPosition, currentPosition);
        } else {
            assetRepository.decrementSortOrderBetween(currentPosition, targetPosition);
        }

        asset.updateSortOrder(targetPosition);
    }

    public DashboardSummaryResponse getDashboardSummary() {
        return DashboardSummaryResponse.from(
                assetRepository.sumAllAmount(),
                assetRepository.sumAllMonthlyPayment(),
                assetRepository.sumAmountByType(AssetType.RETIREMENT),
                assetRepository.sumAmountByType(AssetType.INVESTMENT)
        );
    }

    public DashboardChartResponse getDashboardChart() {
        long total = assetRepository.sumAllAmount();

        List<DashboardChartResponse.ChartItem> items = Arrays.stream(AssetType.values())
                .map(type -> {
                    long amount = assetRepository.sumAmountByType(type);
                    double percentage = total > 0
                            ? Math.round((double) amount / total * 1000.0) / 10.0
                            : 0.0;
                    return DashboardChartResponse.ChartItem.of(type, amount, percentage);
                })
                .toList();

        return DashboardChartResponse.from(items);
    }
}
