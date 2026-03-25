package com.asset.asset_backend.domains.debt.controller;

import com.asset.asset_backend.common.response.ApiPageResponse;
import com.asset.asset_backend.common.enums.AssetType;
import com.asset.asset_backend.common.response.ApiResult;
import com.asset.asset_backend.domains.debt.scheduler.DebtPaymentScheduler;
import com.asset.asset_backend.domains.debt.dto.request.DebtCreateRequest;
import com.asset.asset_backend.domains.debt.dto.request.DebtUpdateRequest;
import com.asset.asset_backend.domains.debt.dto.response.DebtResponse;
import com.asset.asset_backend.domains.debt.dto.response.DebtSummaryResponse;
import com.asset.asset_backend.domains.debt.entity.Debt;
import com.asset.asset_backend.domains.debt.service.DebtService;
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
@RequestMapping("/api/debts")
@RequiredArgsConstructor
public class DebtController {

    private final DebtService debtService;
    private final DebtPaymentScheduler debtPaymentScheduler;

    /**
     * [테스트] 부채 상환 스케줄러 수동 실행
     * POST /api/debts/scheduler/payment
     */
    @PostMapping("/scheduler/payment")
    public ResponseEntity<ApiResult<String>> triggerRepayment() {
        debtPaymentScheduler.processMonthlyRepayments();
        return ResponseEntity.ok(ApiResult.success("부채 상환 스케줄러 실행 완료", " 실행 완료"));
    }

    @PostMapping
    public ResponseEntity<ApiResult<DebtResponse>> createDebt(
            @AuthenticationPrincipal Long userId,
            @RequestBody DebtCreateRequest request) {
        Debt debt = debtService.createDebt(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResult.success(DebtResponse.from(debt), "부채가 추가되었습니다."));
    }

    @GetMapping
    public ResponseEntity<ApiResult<ApiPageResponse<DebtResponse>>> getDebts(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String owner,
            @RequestParam(required = false) AssetType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Debt> debtPage = debtService.searchDebts(category, owner, type, userId, pageable);

        List<DebtResponse> responses = debtPage.getContent().stream()
                .map(DebtResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                ApiResult.success(ApiPageResponse.of(debtPage, responses), "부채 목록을 조회했습니다.")
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResult<DebtResponse>> getDebtById(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {
        Debt debt = debtService.getDebtById(id, userId);
        return ResponseEntity.ok(ApiResult.success(DebtResponse.from(debt), "부채를 조회했습니다."));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResult<DebtResponse>> updateDebt(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @RequestBody DebtUpdateRequest request) {
        Debt debt = debtService.updateDebt(id, request, userId);
        return ResponseEntity.ok(ApiResult.success(DebtResponse.from(debt), "부채가 수정되었습니다."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResult<Void>> deleteDebt(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {
        debtService.deleteDebt(id, userId);
        return ResponseEntity.ok(ApiResult.success(null, "부채가 삭제되었습니다."));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResult<DebtSummaryResponse>> getSummary(
            @AuthenticationPrincipal Long userId) {
        DebtSummaryResponse summary = debtService.getSummary(userId);
        return ResponseEntity.ok(
                ApiResult.success(summary, "부채 요약을 조회했습니다.")
        );
    }
}