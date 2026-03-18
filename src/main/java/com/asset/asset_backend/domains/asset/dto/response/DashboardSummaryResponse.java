package com.asset.asset_backend.domains.asset.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardSummaryResponse {
    private Long totalAmount;           // 총자산
    private Long totalMonthlyPayment;   // 월납입합계
    private Long retirementAmount;      // 노후대비자산 (RETIREMENT 합계)
    private Long investmentAmount;      // 투자자산 (INVESTMENT 합계)

    public static DashboardSummaryResponse from(Long totalAmount, Long totalMonthlyPayment,
                                                Long retirementAmount, Long investmentAmount) {
        return DashboardSummaryResponse.builder()
                .totalAmount(totalAmount)
                .totalMonthlyPayment(totalMonthlyPayment)
                .retirementAmount(retirementAmount)
                .investmentAmount(investmentAmount)
                .build();
    }
}
