package com.asset.asset_backend.domains.asset.repository;

import com.asset.asset_backend.domains.asset.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AssetRepository extends JpaRepository<Asset, Long>, AssetRepositoryCustom {
    List<Asset> findByPaymentDay(Integer paymentDay);
    List<Asset> findByLinkedToInvestmentTrue();
}