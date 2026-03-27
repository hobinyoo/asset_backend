package com.asset.asset_backend.domains.snapshot.repository;

import com.asset.asset_backend.domains.auth.entity.User;
import com.asset.asset_backend.domains.snapshot.entity.InvestmentCategorySnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface InvestmentCategorySnapshotRepository extends JpaRepository<InvestmentCategorySnapshot, Long> {

    List<InvestmentCategorySnapshot> findByUser_IdAndSnapshotDateBetweenOrderBySnapshotDateAsc(
            Long userId, LocalDate startDate, LocalDate endDate);

    List<InvestmentCategorySnapshot> findByUserAndSnapshotDate(User user, LocalDate snapshotDate);

    void deleteByUserAndSnapshotDate(User user, LocalDate snapshotDate);
}