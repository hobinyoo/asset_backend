package com.asset.asset_backend.domains.debt.dto.response;

import com.asset.asset_backend.common.enums.AssetType;

import com.asset.asset_backend.domains.debt.entity.Debt;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DebtResponse {
    private Long id;
    private String category;
    private String owner;
    private Long amount;
    private AssetType type;
    private Long monthlyPayment;
    private Integer paymentDay;
    private String purpose;
    private String note;
    private String createdAt;
    private String updatedAt;

    public static DebtResponse from(Debt debt) {
        return DebtResponse.builder()
                .id(debt.getId())
                .category(debt.getCategory())
                .owner(debt.getOwner())
                .amount(debt.getAmount())
                .type(debt.getType())
                .monthlyPayment(debt.getMonthlyPayment())
                .paymentDay(debt.getPaymentDay())
                .purpose(debt.getPurpose())
                .note(debt.getNote())
                .createdAt(debt.getCreatedAt() != null ? debt.getCreatedAt().toString() : null)
                .updatedAt(debt.getUpdatedAt() != null ? debt.getUpdatedAt().toString() : null)
                .build();
    }
}