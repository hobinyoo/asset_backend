package com.asset.asset_backend.domains.investment.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class InvestmentDashboardSummaryResponse {
    private Long totalAmount;
    private List<CategorySummary> categories;

    public static InvestmentDashboardSummaryResponse from(Long totalAmount, List<CategorySummary> categories) {
        return InvestmentDashboardSummaryResponse.builder()
                .totalAmount(totalAmount)
                .categories(categories)
                .build();
    }

    @Getter
    @Builder
    public static class CategorySummary {
        private String category;
        private Long amount;
        private Double percentage;

        public static CategorySummary of(String category, Long amount, Double percentage) {
            return CategorySummary.builder()
                    .category(category)
                    .amount(amount)
                    .percentage(percentage)
                    .build();
        }
    }
}