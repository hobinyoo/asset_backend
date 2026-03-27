package com.asset.asset_backend.domains.investment.dto.response;

import com.asset.asset_backend.domains.investment.dto.investment.CategoryAmountDto;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class InvestmentDashboardChartResponse {
    private String period;
    private List<ChartDataPoint> data;

    public static InvestmentDashboardChartResponse from(String period, List<ChartDataPoint> data) {
        return InvestmentDashboardChartResponse.builder()
                .period(period)
                .data(data)
                .build();
    }

    @Getter
    @Builder
    public static class ChartDataPoint {
        private LocalDate snapshotDate;
        private List<CategoryAmountDto> categories;

        public static ChartDataPoint of(LocalDate snapshotDate, List<CategoryAmountDto> categories) {
            return ChartDataPoint.builder()
                    .snapshotDate(snapshotDate)
                    .categories(categories)
                    .build();
        }
    }
}
