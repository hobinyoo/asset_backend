package com.asset.asset_backend.domains.asset.repository;

import com.asset.asset_backend.domains.asset.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface AssetRepository extends JpaRepository<Asset, Long>, AssetRepositoryCustom {
    List<Asset> findByPaymentDay(Integer paymentDay);
    List<Asset> findByLinkedToInvestmentTrue();

    // 새 자산 추가 시 맨 뒤 순서 계산용 (데이터 없으면 0 반환)
    @Query("SELECT COALESCE(MAX(a.sortOrder), 0) FROM Asset a")
    Integer findMaxSortOrder();

    // 위로 이동할 때: target ~ current-1 사이 항목들 +1 (밀어내기)
    // ex) 5번 → 2번으로: 2,3,4번이 3,4,5번으로
    @Modifying
    @Query("UPDATE Asset a SET a.sortOrder = a.sortOrder + 1 WHERE a.sortOrder >= :targetPosition AND a.sortOrder < :currentPosition")
    void incrementSortOrderBetween(@Param("targetPosition") Integer targetPosition,
                                   @Param("currentPosition") Integer currentPosition);

    // 아래로 이동할 때: current+1 ~ target 사이 항목들 -1 (당기기)
    // ex) 2번 → 5번으로: 3,4,5번이 2,3,4번으로
    @Modifying
    @Query("UPDATE Asset a SET a.sortOrder = a.sortOrder - 1 WHERE a.sortOrder > :currentPosition AND a.sortOrder <= :targetPosition")
    void decrementSortOrderBetween(@Param("currentPosition") Integer currentPosition,
                                   @Param("targetPosition") Integer targetPosition);
}