package com.asset.asset_backend.domains.asset.dto.response;

import com.asset.asset_backend.common.enums.AssetType;
import com.asset.asset_backend.domains.asset.entity.Asset;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AssetResponse {
    private Long id;
    private String category;
    private String owner;
    private Long amount;
    private AssetType type;
    private Long monthlyPayment;
    private Integer paymentDay;
    private String note;
    private Boolean linkedToInvestment;
    private String createdAt;
    private String updatedAt;

    public static AssetResponse from(Asset asset) {
        return AssetResponse.builder()
                .id(asset.getId())
                .category(asset.getCategory())
                .owner(asset.getOwner())
                .amount(asset.getAmount())
                .type(asset.getType())
                .monthlyPayment(asset.getMonthlyPayment())
                .paymentDay(asset.getPaymentDay())
                .note(asset.getNote())
                .linkedToInvestment(asset.getLinkedToInvestment())
                .createdAt(asset.getCreatedAt() != null ? asset.getCreatedAt().toString() : null)
                .updatedAt(asset.getUpdatedAt() != null ? asset.getUpdatedAt().toString() : null)
                .build();
    }
}