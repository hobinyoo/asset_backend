package com.asset.asset_backend.domains.investment.dto.response;

import com.asset.asset_backend.domains.investment.entity.Investment;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InvestmentResponse {
    private Long id;
    private Long assetId;
    private String account;         // asset.category 에서 항상 최신값으로
    private String category;
    private String stockName;
    private String ticker;
    private String owner;
    private Long purchasePrice;
    private Long quantity;
    private Long purchaseAmount;
    private Long currentPrice;
    private Long evaluationAmount;
    private Double profitRate;
    private String createdAt;
    private String updatedAt;

    public static InvestmentResponse from(Investment inv, Long currentPrice) {
        Long buyAmt = inv.getPurchaseAmount() != null
                ? inv.getPurchaseAmount()
                : (inv.getPurchasePrice() != null && inv.getQuantity() != null
                ? inv.getPurchasePrice() * inv.getQuantity() : null);

        Long evaluationAmount = null;
        if (currentPrice != null) {
            evaluationAmount = inv.getQuantity() != null
                    ? currentPrice * inv.getQuantity()
                    : currentPrice;
        }

        Double profitRate = null;
        if (buyAmt != null && buyAmt > 0 && evaluationAmount != null) {
            profitRate = (double)(evaluationAmount - buyAmt) / buyAmt * 100;
        }

        return InvestmentResponse.builder()
                .id(inv.getId())
                .assetId(inv.getAsset() != null ? inv.getAsset().getId() : null)
                .account(inv.getAsset() != null ? inv.getAsset().getCategory() : null) // 항상 최신값
                .category(inv.getCategory())
                .stockName(inv.getStockName())
                .ticker(inv.getTicker())
                .owner(inv.getOwner())
                .purchasePrice(inv.getPurchasePrice())
                .quantity(inv.getQuantity())
                .purchaseAmount(buyAmt)
                .currentPrice(currentPrice)
                .evaluationAmount(evaluationAmount)
                .profitRate(profitRate)
                .createdAt(inv.getCreatedAt() != null ? inv.getCreatedAt().toString() : null)
                .updatedAt(inv.getUpdatedAt() != null ? inv.getUpdatedAt().toString() : null)
                .build();
    }
}