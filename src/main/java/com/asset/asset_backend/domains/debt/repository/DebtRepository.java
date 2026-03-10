package com.asset.asset_backend.domains.debt.repository;
import com.asset.asset_backend.domains.debt.entity.Debt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DebtRepository extends JpaRepository<Debt, Long>, DebtRepositoryCustom {
    List<Debt> findByPaymentDay(Integer paymentDay);
}