package com.asset.asset_backend.domains.snapshot.repository;

import com.asset.asset_backend.domains.auth.entity.User;
import com.asset.asset_backend.domains.snapshot.entity.AssetDailySnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AssetDailySnapshotRepository extends JpaRepository<AssetDailySnapshot, Long> {
    List<AssetDailySnapshot> findByUser_IdAndSnapshotDateBetweenOrderBySnapshotDateAsc(
            Long userId, LocalDate startDate, LocalDate endDate);

    Optional<AssetDailySnapshot> findByUserAndSnapshotDate(User user, LocalDate snapshotDate);
}
