package com.asset.asset_backend.domains.asset.controller;

import com.asset.asset_backend.common.enums.AssetType;
import com.asset.asset_backend.common.response.ApiPageResponse;
import com.asset.asset_backend.common.response.ApiResult;
import com.asset.asset_backend.domains.asset.dto.request.AssetCreateRequest;
import com.asset.asset_backend.domains.asset.dto.request.AssetReorderRequest;
import com.asset.asset_backend.domains.asset.dto.request.AssetUpdateRequest;
import com.asset.asset_backend.domains.asset.dto.response.AssetResponse;
import com.asset.asset_backend.domains.asset.dto.response.DashboardChartResponse;
import com.asset.asset_backend.domains.asset.dto.response.DashboardSummaryResponse;
import com.asset.asset_backend.domains.asset.entity.Asset;
import com.asset.asset_backend.domains.asset.service.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    /**
     * 자산 생성
     * POST /api/assets
     */
    @PostMapping
    public ResponseEntity<ApiResult<AssetResponse>> createAsset(
            @AuthenticationPrincipal Long userId,
            @RequestBody AssetCreateRequest request) {
        Asset asset = assetService.createAsset(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResult.success(AssetResponse.from(asset), "자산이 추가되었습니다."));
    }

    /**
     * 자산 목록 조회 (페이징 + 필터)
     * GET /api/assets?owner=호빈&type=HOUSING&page=0&size=10
     */
    @GetMapping
    public ResponseEntity<ApiResult<ApiPageResponse<AssetResponse>>> getAssets(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String owner,
            @RequestParam(required = false) AssetType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Asset> assetPage = assetService.searchAssets(category, owner, type, userId, pageable);

        List<AssetResponse> responses = assetPage.getContent().stream()
                .map(AssetResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                ApiResult.success(ApiPageResponse.of(assetPage, responses), "자산 목록을 조회했습니다.")
        );
    }

    /**
     * 자산 단건 조회
     * GET /api/assets/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResult<AssetResponse>> getAssetById(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {
        Asset asset = assetService.getAssetById(id, userId);
        return ResponseEntity.ok(ApiResult.success(AssetResponse.from(asset), "자산을 조회했습니다."));
    }

    /**
     * 자산 수정
     * PUT /api/assets/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResult<AssetResponse>> updateAsset(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @RequestBody AssetUpdateRequest request) {
        Asset asset = assetService.updateAsset(id, request, userId);
        return ResponseEntity.ok(ApiResult.success(AssetResponse.from(asset), "자산이 수정되었습니다."));
    }

    /**
     * 자산 삭제
     * DELETE /api/assets/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResult<Void>> deleteAsset(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {
        assetService.deleteAsset(id, userId);
        return ResponseEntity.ok(ApiResult.success(null, "자산이 삭제되었습니다."));
    }

    @GetMapping("/linked")
    public ResponseEntity<ApiResult<List<AssetResponse>>> getLinkedAssets(
            @AuthenticationPrincipal Long userId) {
        List<AssetResponse> responses = assetService.getLinkedAssets(userId).stream()
                .map(AssetResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResult.success(responses, "투자 연동 자산 목록을 조회했습니다."));
    }

    /**
     * 자산 순서 변경
     * PATCH /api/assets/{id}/reorder
     */
    @PatchMapping("/{id}/reorder")
    public ResponseEntity<ApiResult<Void>> reorderAsset(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @RequestBody AssetReorderRequest request) {
        assetService.reorderAsset(id, request, userId);
        return ResponseEntity.ok(ApiResult.success(null, "순서가 변경되었습니다."));
    }

    /**
     * 대시보드 요약 조회
     * GET /api/assets/dashboard/summary
     */
    @GetMapping("/dashboard/summary")
    public ResponseEntity<ApiResult<DashboardSummaryResponse>> getDashboardSummary(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(
                ApiResult.success(assetService.getDashboardSummary(userId), "대시보드 요약을 조회했습니다.")
        );
    }

    /**
     * 대시보드 차트 데이터 조회
     * GET /api/assets/dashboard/chart
     */
    @GetMapping("/dashboard/chart")
    public ResponseEntity<ApiResult<DashboardChartResponse>> getDashboardChart(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(
                ApiResult.success(assetService.getDashboardChart(userId), "대시보드 차트 데이터를 조회했습니다.")
        );
    }
}