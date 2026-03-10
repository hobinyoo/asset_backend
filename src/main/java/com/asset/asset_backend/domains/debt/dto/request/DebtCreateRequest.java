package com.asset.asset_backend.domains.debt.dto.request;

import com.asset.asset_backend.common.enums.AssetType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DebtCreateRequest {
    private String category;
    private String owner;
    private Long amount;
    private AssetType type;
    private Long monthlyPayment;
    private Integer paymentDay;
    private String purpose;
    private String note;
}