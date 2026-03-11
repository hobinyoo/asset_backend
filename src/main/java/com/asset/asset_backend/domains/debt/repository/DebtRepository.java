package com.asset.asset_backend.domains.debt.repository;
import com.asset.asset_backend.domains.debt.entity.Debt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DebtRepository extends JpaRepository<Debt, Long>, DebtRepositoryCustom {
    List<Debt> findByPaymentDay(Integer paymentDay);

    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM Debt d")
    Long sumAllAmount();

    @Query("SELECT COALESCE(SUM(d.monthlyPayment), 0) FROM Debt d WHERE d.monthlyPayment IS NOT NULL")
    Long sumAllMonthlyPayment();
}