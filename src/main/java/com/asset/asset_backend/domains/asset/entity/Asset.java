package com.asset.asset_backend.domains.asset.entity;

import com.asset.asset_backend.domains.auth.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.asset.asset_backend.common.enums.AssetType;
import java.time.LocalDateTime;

@Entity
@Table(name = "asset")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String category;            // 전세보증금, 청년도약적금, IRP계좌 등

    @Column(nullable = false)
    private String owner;               // 호빈, 선주, 공동

    @Column(nullable = false)
    private Long amount;                // 현재 금액

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetType type;             // FIXED, REGULAR, VARIABLE

    private Long monthlyPayment;        // 납입금액(월), 없으면 null

    private Integer paymentDay;         // 월 납입일 (1~31), 없으면 null

    private String note;                // 비고

    private Boolean linkedToInvestment; // 투자 시트 연결 여부 (IRP, KB증권 등)

    @Column(nullable = false)
    private Integer sortOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // ✅ 생성 메서드
    public static Asset createAsset(String category, String owner, Long amount,
                                    AssetType type, Long monthlyPayment, Integer paymentDay,
                                    String note, Boolean linkedToInvestment, Integer sortOrder,
                                    User user) {
        Asset asset = new Asset();
        asset.category = category;
        asset.owner = owner;
        asset.amount = amount;
        asset.type = type;
        asset.monthlyPayment = monthlyPayment;
        asset.paymentDay = paymentDay;
        asset.note = note;
        asset.linkedToInvestment = linkedToInvestment != null && linkedToInvestment;
        asset.sortOrder = sortOrder;
        asset.user = user;
        return asset;
    }

    public void updateAssetInfo(String category, String owner, Long amount,
                                AssetType type, Long monthlyPayment, Integer paymentDay,
                                String note, Boolean linkedToInvestment) {
        if (category != null) this.category = category;
        if (owner != null) this.owner = owner;
        if (amount != null) this.amount = amount;
        if (type != null) this.type = type;
        if (monthlyPayment != null) this.monthlyPayment = monthlyPayment;
        if (paymentDay != null) this.paymentDay = paymentDay;
        if (note != null) this.note = note;
        if (linkedToInvestment != null) this.linkedToInvestment = linkedToInvestment;
    }

    public void updateSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void addMonthlyPayment() {
        if (this.monthlyPayment != null) {
            this.amount += this.monthlyPayment;
        }
    }

    public void updateAmount(Long amount) {
        this.amount = amount;
    }
}