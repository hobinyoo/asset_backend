package com.asset.asset_backend.domains.investment.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class InvestmentCreateRequest {
    private Long assetId;
    private String category;
    private String stockName;
    private String ticker;
    private String owner;
    private Long purchasePrice;
    private Long quantity;
    private Long purchaseAmount;
}