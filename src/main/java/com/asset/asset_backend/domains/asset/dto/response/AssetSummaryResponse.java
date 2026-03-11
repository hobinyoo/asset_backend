package com.asset.asset_backend.domains.asset.dto.response;

import com.asset.asset_backend.domains.debt.dto.response.DebtSummaryResponse;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AssetSummaryResponse {
    private Long totalAmount;
    private Long totalMonthlyPayment;

    public static AssetSummaryResponse from(Long totalAmount, Long totalMonthlyPayment) {
        return AssetSummaryResponse.builder()
                .totalAmount(totalAmount)
                .totalMonthlyPayment(totalMonthlyPayment)
                .build();
    }
}