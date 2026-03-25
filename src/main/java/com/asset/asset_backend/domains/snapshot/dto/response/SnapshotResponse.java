package com.asset.asset_backend.domains.snapshot.dto.response;

import com.asset.asset_backend.domains.snapshot.entity.AssetDailySnapshot;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class SnapshotResponse {
    private LocalDate snapshotDate;
    private Long totalAssetAmount;
    private Long retirementAmount;
    private Long investmentAmount;
    private Long totalDebtAmount;
    private Long netWorthAmount;

    public static SnapshotResponse from(AssetDailySnapshot snapshot) {
        return SnapshotResponse.builder()
                .snapshotDate(snapshot.getSnapshotDate())
                .totalAssetAmount(snapshot.getTotalAssetAmount())
                .retirementAmount(snapshot.getRetirementAmount())
                .investmentAmount(snapshot.getInvestmentAmount())
                .totalDebtAmount(snapshot.getTotalDebtAmount())
                .netWorthAmount(snapshot.getNetWorthAmount())
                .build();
    }
}
