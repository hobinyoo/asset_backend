package com.asset.asset_backend.domains.debt.repository;

import com.asset.asset_backend.domains.debt.entity.Debt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DebtRepository extends JpaRepository<Debt, Long>, DebtRepositoryCustom {
    List<Debt> findByPaymentDay(Integer paymentDay);

    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM Debt d WHERE d.user.id = :userId")
    Long sumAllAmountByMemberId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(d.monthlyPayment), 0) FROM Debt d WHERE d.monthlyPayment IS NOT NULL AND d.user.id = :userId")
    Long sumAllMonthlyPaymentByMemberId(@Param("userId") Long userId);
}
