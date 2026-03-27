package com.asset.asset_backend.domains.investment.repository;

import com.asset.asset_backend.common.enums.AssetType;
import com.asset.asset_backend.domains.investment.entity.Investment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvestmentRepository extends JpaRepository<Investment, Long>, InvestmentRepositoryCustom {
    List<Investment> findByAssetId(Long assetId);
    List<Investment> findByAsset_UserId(Long userId);
    List<Investment> findByAsset_TypeAndAsset_User_Id(AssetType assetType, Long userId);
}
