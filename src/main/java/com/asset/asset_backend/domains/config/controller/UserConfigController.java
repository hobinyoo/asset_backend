package com.asset.asset_backend.domains.config.controller;

import com.asset.asset_backend.common.response.ApiResult;
import com.asset.asset_backend.domains.config.dto.request.UserConfigRequest;
import com.asset.asset_backend.domains.config.dto.response.UserConfigResponse;
import com.asset.asset_backend.domains.config.entity.ConfigType;
import com.asset.asset_backend.domains.config.entity.UserConfig;
import com.asset.asset_backend.domains.config.service.UserConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class UserConfigController {

    private final UserConfigService userConfigService;

    // ─── Asset Category ──────────────────────────────────────────────────────────

    @GetMapping("/asset-categories")
    public ResponseEntity<ApiResult<List<UserConfigResponse>>> getAssetCategories(
            @AuthenticationPrincipal Long userId) {
        List<UserConfigResponse> responses = userConfigService.getConfigs(userId, ConfigType.ASSET_CATEGORY).stream()
                .map(UserConfigResponse::from).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResult.success(responses, "자산 카테고리 목록을 조회했습니다."));
    }

    @PostMapping("/asset-categories")
    public ResponseEntity<ApiResult<UserConfigResponse>> addAssetCategory(
            @AuthenticationPrincipal Long userId,
            @RequestBody UserConfigRequest request) {
        UserConfig config = userConfigService.addConfig(userId, ConfigType.ASSET_CATEGORY, request.getValue());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResult.success(UserConfigResponse.from(config), "자산 카테고리가 추가되었습니다."));
    }

    @DeleteMapping("/asset-categories/{id}")
    public ResponseEntity<ApiResult<Void>> deleteAssetCategory(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {
        userConfigService.deleteConfig(id, userId);
        return ResponseEntity.ok(ApiResult.success(null, "자산 카테고리가 삭제되었습니다."));
    }

    // ─── Asset Owner ─────────────────────────────────────────────────────────────

    @GetMapping("/asset-owners")
    public ResponseEntity<ApiResult<List<UserConfigResponse>>> getAssetOwners(
            @AuthenticationPrincipal Long userId) {
        List<UserConfigResponse> responses = userConfigService.getConfigs(userId, ConfigType.ASSET_OWNER).stream()
                .map(UserConfigResponse::from).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResult.success(responses, "소유자 목록을 조회했습니다."));
    }

    @PostMapping("/asset-owners")
    public ResponseEntity<ApiResult<UserConfigResponse>> addAssetOwner(
            @AuthenticationPrincipal Long userId,
            @RequestBody UserConfigRequest request) {
        UserConfig config = userConfigService.addConfig(userId, ConfigType.ASSET_OWNER, request.getValue());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResult.success(UserConfigResponse.from(config), "소유자가 추가되었습니다."));
    }

    @DeleteMapping("/asset-owners/{id}")
    public ResponseEntity<ApiResult<Void>> deleteAssetOwner(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {
        userConfigService.deleteConfig(id, userId);
        return ResponseEntity.ok(ApiResult.success(null, "소유자가 삭제되었습니다."));
    }

    // ─── Investment Category ──────────────────────────────────────────────────────

    @GetMapping("/investment-categories")
    public ResponseEntity<ApiResult<List<UserConfigResponse>>> getInvestmentCategories(
            @AuthenticationPrincipal Long userId) {
        List<UserConfigResponse> responses = userConfigService.getConfigs(userId, ConfigType.INVESTMENT_CATEGORY).stream()
                .map(UserConfigResponse::from).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResult.success(responses, "투자 카테고리 목록을 조회했습니다."));
    }

    @PostMapping("/investment-categories")
    public ResponseEntity<ApiResult<UserConfigResponse>> addInvestmentCategory(
            @AuthenticationPrincipal Long userId,
            @RequestBody UserConfigRequest request) {
        UserConfig config = userConfigService.addConfig(userId, ConfigType.INVESTMENT_CATEGORY, request.getValue());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResult.success(UserConfigResponse.from(config), "투자 카테고리가 추가되었습니다."));
    }

    @DeleteMapping("/investment-categories/{id}")
    public ResponseEntity<ApiResult<Void>> deleteInvestmentCategory(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {
        userConfigService.deleteConfig(id, userId);
        return ResponseEntity.ok(ApiResult.success(null, "투자 카테고리가 삭제되었습니다."));
    }

}