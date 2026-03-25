package com.asset.asset_backend.domains.investment.repository;

import com.asset.asset_backend.domains.investment.entity.Investment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InvestmentRepositoryCustom {
    Page<Investment> searchInvestments(String owner, String category, Long assetId, Long userId, Pageable pageable);
}