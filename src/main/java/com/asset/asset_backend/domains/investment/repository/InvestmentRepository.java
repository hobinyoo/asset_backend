package com.asset.asset_backend.domains.investment.repository;

import com.asset.asset_backend.domains.investment.entity.Investment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvestmentRepository extends JpaRepository<Investment, Long>, InvestmentRepositoryCustom {
    List<Investment> findByAssetId(Long assetId);   // 자산 연결 조회

}