package com.asset.asset_backend.common.scheduler;

import com.asset.asset_backend.domains.debt.entity.Debt;

import com.asset.asset_backend.domains.debt.repository.DebtRepository;
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
public class DebtPaymentScheduler {

    private final DebtRepository debtRepository;

    @Scheduled(cron = "0 0 0 * * *") // 매일 자정
    @Transactional
    public void processMonthlyRepayments() {
        int today = LocalDate.now().getDayOfMonth();
        log.info("[DebtPaymentScheduler] 실행 - 오늘: {}일", today);

        List<Debt> debts = debtRepository.findByPaymentDay(today);
        for (Debt debt : debts) {
            if (debt.getMonthlyPayment() != null) {
                debt.repayMonthly();
                log.info("[DebtPaymentScheduler] 상환 처리 - {} -{}원", debt.getCategory(), debt.getMonthlyPayment());
            }
        }
    }
}