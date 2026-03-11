package com.asset.asset_backend.domains.investment.controller;

import com.asset.asset_backend.common.response.ApiPageResponse;
import com.asset.asset_backend.common.response.ApiResult;
import com.asset.asset_backend.domains.investment.dto.request.InvestmentCreateRequest;
import com.asset.asset_backend.domains.investment.dto.request.InvestmentUpdateRequest;
import com.asset.asset_backend.domains.investment.dto.response.InvestmentResponse;
import com.asset.asset_backend.domains.investment.service.InvestmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/investments")
@RequiredArgsConstructor
public class InvestmentController {

    private final InvestmentService investmentService;

    @PostMapping
    public ResponseEntity<ApiResult<InvestmentResponse>> createInvestment(
            @RequestBody InvestmentCreateRequest request) {

        InvestmentResponse response = investmentService.createInvestment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResult.success(response, "투자 항목이 추가되었습니다."));
    }

    @GetMapping
    public ResponseEntity<ApiResult<ApiPageResponse<InvestmentResponse>>> getInvestments(
            @RequestParam(required = false) String owner,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long  assetId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<InvestmentResponse> investmentPage = investmentService.searchInvestments(owner, category, assetId, pageable);

        return ResponseEntity.ok(
                ApiResult.success(ApiPageResponse.of(investmentPage, investmentPage.getContent()), "투자 목록을 조회했습니다.")
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResult<InvestmentResponse>> getInvestmentById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResult.success(investmentService.getInvestmentById(id), "투자 항목을 조회했습니다."));
    }

    @GetMapping("/asset/{assetId}")
    public ResponseEntity<ApiResult<List<InvestmentResponse>>> getInvestmentsByAssetId(
            @PathVariable Long assetId) {
        return ResponseEntity.ok(ApiResult.success(investmentService.getInvestmentsByAssetId(assetId), "자산 연결 투자 목록을 조회했습니다."));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResult<InvestmentResponse>> updateInvestment(
            @PathVariable Long id,
            @RequestBody InvestmentUpdateRequest request) {

        return ResponseEntity.ok(ApiResult.success(investmentService.updateInvestment(id, request), "투자 항목이 수정되었습니다."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResult<Void>> deleteInvestment(@PathVariable Long id) {
        investmentService.deleteInvestment(id);
        return ResponseEntity.ok(ApiResult.success(null, "투자 항목이 삭제되었습니다."));
    }

    @PostMapping("/sync-asset/{assetId}")
    public ResponseEntity<ApiResult<Void>> syncAssetAmount(@PathVariable Long assetId) {
        investmentService.syncAssetAmount(assetId);
        return ResponseEntity.ok(ApiResult.success(null, "자산 금액이 업데이트되었습니다."));
    }
}