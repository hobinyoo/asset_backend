package com.asset.asset_backend.common.scheduler;

import com.asset.asset_backend.domains.asset.entity.Asset;
import com.asset.asset_backend.domains.asset.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AssetPaymentScheduler {

    private final AssetRepository assetRepository;

    @Scheduled(cron = "0 0 0 * * *") // 매일 자정
    @Transactional
    public void processMonthlyPayments() {
        int today = LocalDate.now().getDayOfMonth();
        log.info("[AssetPaymentScheduler] 실행 - 오늘: {}일", today);

        List<Asset> assets = assetRepository.findByPaymentDay(today);
        for (Asset asset : assets) {
            if (asset.getMonthlyPayment() != null) {
                asset.addMonthlyPayment();
                log.info("[AssetPaymentScheduler] 납입 처리 - {} +{}원", asset.getCategory(), asset.getMonthlyPayment());
            }
        }
    }
}