package com.asset.asset_backend.domains.debt.repository;

import com.asset.asset_backend.common.enums.AssetType;
import com.asset.asset_backend.domains.debt.entity.Debt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DebtRepositoryCustom {
    Page<Debt> searchDebts(String category, String owner, AssetType type, Long userId, Pageable pageable);
}