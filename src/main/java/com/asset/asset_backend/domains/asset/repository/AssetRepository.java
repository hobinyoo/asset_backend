package com.asset.asset_backend.domains.asset.repository;

import com.asset.asset_backend.common.enums.AssetType;
import com.asset.asset_backend.domains.asset.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface AssetRepository extends JpaRepository<Asset, Long>, AssetRepositoryCustom {
    List<Asset> findByPaymentDay(Integer paymentDay);
    List<Asset> findByLinkedToInvestmentTrue();
    List<Asset> findByLinkedToInvestmentTrueAndUser_Id(Long userId);

    // 스케줄러용 (전체 집계)
    @Query("SELECT COALESCE(SUM(a.amount), 0) FROM Asset a")
    Long sumAllAmount();

    @Query("SELECT COALESCE(SUM(a.amount), 0) FROM Asset a WHERE a.type = :type")
    Long sumAmountByType(@Param("type") AssetType type);

    // 유저별 집계
    @Query("SELECT COALESCE(MAX(a.sortOrder), 0) FROM Asset a WHERE a.user.id = :userId")
    Integer findMaxSortOrderByMemberId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Asset a SET a.sortOrder = a.sortOrder + 1 WHERE a.sortOrder >= :targetPosition AND a.sortOrder < :currentPosition AND a.user.id = :userId")
    void incrementSortOrderBetween(@Param("targetPosition") Integer targetPosition,
                                   @Param("currentPosition") Integer currentPosition,
                                   @Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Asset a SET a.sortOrder = a.sortOrder - 1 WHERE a.sortOrder > :currentPosition AND a.sortOrder <= :targetPosition AND a.user.id = :userId")
    void decrementSortOrderBetween(@Param("currentPosition") Integer currentPosition,
                                   @Param("targetPosition") Integer targetPosition,
                                   @Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(a.amount), 0) FROM Asset a WHERE a.user.id = :userId")
    Long sumAllAmountByMemberId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(a.monthlyPayment), 0) FROM Asset a WHERE a.monthlyPayment IS NOT NULL AND a.user.id = :userId")
    Long sumAllMonthlyPaymentByMemberId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(a.amount), 0) FROM Asset a WHERE a.type = :type AND a.user.id = :userId")
    Long sumAmountByTypeAndMemberId(@Param("type") AssetType type, @Param("userId") Long userId);
}
