package com.asset.asset_backend.domains.investment.dto.response;

import com.asset.asset_backend.common.enums.MarketType;
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
    private MarketType marketType;
    private Long currentPrice;      // 원본 가격 (OVERSEAS: USD, DOMESTIC: KRW)
    private Double exchangeRate;    // USD→KRW 환율 (OVERSEAS만, 나머지 null)
    private Long evaluationAmount;  // 항상 KRW
    private Double profitRate;
    private String createdAt;
    private String updatedAt;

    public static InvestmentResponse from(Investment inv, Long currentPrice, Double exchangeRate) {
        Long buyAmt = inv.getPurchaseAmount() != null
                ? inv.getPurchaseAmount()
                : (inv.getPurchasePrice() != null && inv.getQuantity() != null
                ? inv.getPurchasePrice() * inv.getQuantity() : null);

        // OVERSEAS: currentPrice(USD) * exchangeRate → KRW로 환산
        Long currentPriceKrw = currentPrice;
        if (inv.getMarketType() == MarketType.OVERSEAS && currentPrice != null && exchangeRate != null) {
            currentPriceKrw = Math.round(currentPrice * exchangeRate);
        }

        Long evaluationAmount = null;
        if (currentPriceKrw != null) {
            evaluationAmount = inv.getQuantity() != null
                    ? currentPriceKrw * inv.getQuantity()
                    : currentPriceKrw;
        }

        Double profitRate = null;
        if (buyAmt != null && buyAmt > 0 && evaluationAmount != null) {
            profitRate = Math.round((double)(evaluationAmount - buyAmt) / buyAmt * 10000.0) / 100.0;
        }

        return InvestmentResponse.builder()
                .id(inv.getId())
                .assetId(inv.getAsset() != null ? inv.getAsset().getId() : null)
                .account(inv.getAsset() != null ? inv.getAsset().getCategory() : null)
                .category(inv.getCategory())
                .stockName(inv.getStockName())
                .ticker(inv.getTicker())
                .owner(inv.getOwner())
                .purchasePrice(inv.getPurchasePrice())
                .quantity(inv.getQuantity())
                .purchaseAmount(buyAmt)
                .marketType(inv.getMarketType())
                .currentPrice(currentPrice)
                .exchangeRate(inv.getMarketType() == MarketType.OVERSEAS ? exchangeRate : null)
                .evaluationAmount(evaluationAmount)
                .profitRate(profitRate)
                .createdAt(inv.getCreatedAt() != null ? inv.getCreatedAt().toString() : null)
                .updatedAt(inv.getUpdatedAt() != null ? inv.getUpdatedAt().toString() : null)
                .build();
    }
}
