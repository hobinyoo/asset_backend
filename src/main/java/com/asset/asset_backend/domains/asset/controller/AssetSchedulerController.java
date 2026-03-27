package com.asset.asset_backend.domains.asset.controller;

import com.asset.asset_backend.common.response.ApiResult;
import com.asset.asset_backend.domains.asset.scheduler.AssetPaymentScheduler;
import com.asset.asset_backend.domains.asset.scheduler.AssetScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/scheduler")
@RequiredArgsConstructor
public class AssetSchedulerController {

    private final AssetScheduler assetScheduler;
    private final AssetPaymentScheduler assetPaymentScheduler;

    @PostMapping("/daily-snapshot")
    public ResponseEntity<ApiResult<Void>> runDailySnapshot() {
        assetScheduler.saveDailySnapshot();
        return ResponseEntity.ok(ApiResult.success(null, "스냅샷 저장 완료"));
    }

    @PostMapping("/payment")
    public ResponseEntity<ApiResult<Void>> runPayment() {
        assetPaymentScheduler.processMonthlyPayments();
        return ResponseEntity.ok(ApiResult.success(null, "자산 납입 스케줄러 실행 완료"));
    }
}