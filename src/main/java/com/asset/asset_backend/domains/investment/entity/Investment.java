package com.asset.asset_backend.domains.investment.entity;

import com.asset.asset_backend.common.enums.MarketType;
import com.asset.asset_backend.domains.asset.entity.Asset;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "investment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Investment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id")
    private Asset asset;

    @Column(nullable = false)
    private String category;        // ETF, 금, 현금 등

    @Column(nullable = false)
    private String stockName;       // S&P500, 나스닥100 등

    private String ticker;          // SPY, QQQ, 411060.KS 등

    @Column(nullable = false)
    private String owner;           // 호빈, 선주

    private Long purchasePrice;     // 매수단가 (1주당)
    private Long quantity;          // 수량
    private Long purchaseAmount;    // 매수금액

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MarketType marketType;  // 국내/해외

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public static Investment createInvestment(Asset asset, String category, String stockName,
                                              String ticker, String owner, Long purchasePrice,
                                              Long quantity, Long purchaseAmount, MarketType marketType) {
        Investment investment = new Investment();
        investment.asset = asset;
        investment.category = category;
        investment.stockName = stockName;
        investment.ticker = ticker;
        investment.owner = owner;
        investment.purchasePrice = purchasePrice;
        investment.quantity = quantity;
        investment.purchaseAmount = purchaseAmount;
        investment.marketType = marketType;
        return investment;
    }

    public void updateInvestmentInfo(Asset asset, String category, String stockName, String ticker,
                                     String owner, Long purchasePrice, Long quantity, Long purchaseAmount,
                                     MarketType marketType) {
        if (asset != null) this.asset = asset;
        if (category != null) this.category = category;
        if (stockName != null) this.stockName = stockName;
        if (ticker != null) this.ticker = ticker;
        if (owner != null) this.owner = owner;
        if (purchasePrice != null) this.purchasePrice = purchasePrice;
        if (quantity != null) this.quantity = quantity;
        if (purchaseAmount != null) this.purchaseAmount = purchaseAmount;
        if (marketType != null) this.marketType = marketType;
    }
}