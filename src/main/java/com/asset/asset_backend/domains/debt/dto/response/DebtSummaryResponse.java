package com.asset.asset_backend.domains.debt.dto.response;

import com.asset.asset_backend.domains.debt.entity.Debt;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DebtSummaryResponse {
    private Long totalAmount;
    private Long totalMonthlyPayment;

    public static DebtSummaryResponse from(Long totalAmount, Long totalMonthlyPayment) {
        return DebtSummaryResponse.builder()
                .totalAmount(totalAmount)
                .totalMonthlyPayment(totalMonthlyPayment)
                .build();
    }
}