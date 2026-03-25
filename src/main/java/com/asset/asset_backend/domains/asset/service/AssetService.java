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
import com.asset.asset_backend.domains.auth.entity.User;
import com.asset.asset_backend.domains.auth.repository.UserRepository;
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
    private final UserRepository userRepository;

    @Transactional
    public Asset createAsset(AssetCreateRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND, "User ID: " + userId));
        Integer maxSortOrder = assetRepository.findMaxSortOrderByMemberId(userId);
        Asset asset = Asset.createAsset(
                request.getCategory(),
                request.getOwner(),
                request.getAmount(),
                request.getType(),
                request.getMonthlyPayment(),
                request.getPaymentDay(),
                request.getNote(),
                request.getLinkedToInvestment(),
                maxSortOrder + 1,
                user
        );
        return assetRepository.save(asset);
    }

    public Page<Asset> searchAssets(String category, String owner, AssetType type, Long userId, Pageable pageable) {
        return assetRepository.searchAssets(category, owner, type, userId, pageable);
    }

    public Asset getAssetById(Long id, Long userId) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Asset ID: " + id));
        validateOwnership(asset.getUser().getId(), userId);
        return asset;
    }

    @Transactional
    public Asset updateAsset(Long id, AssetUpdateRequest request, Long userId) {
        Asset asset = getAssetById(id, userId);
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
    public void deleteAsset(Long id, Long userId) {
        Asset asset = getAssetById(id, userId);
        assetRepository.delete(asset);
    }

    public List<Asset> getLinkedAssets(Long userId) {
        return assetRepository.findByLinkedToInvestmentTrueAndUser_Id(userId);
    }

    @Transactional
    public void reorderAsset(Long id, AssetReorderRequest request, Long userId) {
        Asset asset = getAssetById(id, userId);
        Integer currentPosition = asset.getSortOrder();
        Integer targetPosition = request.getTargetPosition();

        if (currentPosition.equals(targetPosition)) return;

        if (currentPosition > targetPosition) {
            assetRepository.incrementSortOrderBetween(targetPosition, currentPosition, userId);
        } else {
            assetRepository.decrementSortOrderBetween(currentPosition, targetPosition, userId);
        }

        asset.updateSortOrder(targetPosition);
    }

    public DashboardSummaryResponse getDashboardSummary(Long userId) {
        return DashboardSummaryResponse.from(
                assetRepository.sumAllAmountByMemberId(userId),
                assetRepository.sumAllMonthlyPaymentByMemberId(userId),
                assetRepository.sumAmountByTypeAndMemberId(AssetType.RETIREMENT, userId),
                assetRepository.sumAmountByTypeAndMemberId(AssetType.INVESTMENT, userId)
        );
    }

    public DashboardChartResponse getDashboardChart(Long userId) {
        long total = assetRepository.sumAllAmountByMemberId(userId);

        List<DashboardChartResponse.ChartItem> items = Arrays.stream(AssetType.values())
                .map(type -> {
                    long amount = assetRepository.sumAmountByTypeAndMemberId(type, userId);
                    double percentage = total > 0
                            ? Math.round((double) amount / total * 1000.0) / 10.0
                            : 0.0;
                    return DashboardChartResponse.ChartItem.of(type, amount, percentage);
                })
                .toList();

        return DashboardChartResponse.from(items);
    }

    private void validateOwnership(Long assetUserId, Long userId) {
        if (!assetUserId.equals(userId)) {
            throw new BaseException(ErrorCode.FORBIDDEN, "해당 자산에 접근 권한이 없습니다");
        }
    }
}
