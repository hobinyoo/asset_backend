package com.asset.asset_backend.domains.asset.dto.response;

import com.asset.asset_backend.common.enums.AssetType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DashboardChartResponse {
    private List<ChartItem> items;

    public static DashboardChartResponse from(List<ChartItem> items) {
        return DashboardChartResponse.builder()
                .items(items)
                .build();
    }

    @Getter
    @Builder
    public static class ChartItem {
        private AssetType type;
        private Long amount;
        private Double percentage;  // 전체 대비 비율 (소수점 1자리)

        public static ChartItem of(AssetType type, Long amount, Double percentage) {
            return ChartItem.builder()
                    .type(type)
                    .amount(amount)
                    .percentage(percentage)
                    .build();
        }
    }
}
