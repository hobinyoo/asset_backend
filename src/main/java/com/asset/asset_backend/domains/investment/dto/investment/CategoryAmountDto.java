package com.asset.asset_backend.domains.investment.dto.investment;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CategoryAmountDto {
    private String category;
    private Long amount;

    public static CategoryAmountDto of(String category, Long amount) {
        return CategoryAmountDto.builder()
                .category(category)
                .amount(amount)
                .build();
    }
}
