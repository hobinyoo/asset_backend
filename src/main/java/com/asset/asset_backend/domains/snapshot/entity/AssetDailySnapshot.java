package com.asset.asset_backend.domains.snapshot.entity;

import com.asset.asset_backend.domains.auth.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "asset_daily_snapshot")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AssetDailySnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate snapshotDate;

    @Column(nullable = false)
    private Long totalAssetAmount;

    @Column(nullable = false)
    private Long retirementAmount;

    @Column(nullable = false)
    private Long investmentAmount;

    @Column(nullable = false)
    private Long totalDebtAmount;

    @Column(nullable = false)
    private Long netWorthAmount;    // totalAssetAmount - totalDebtAmount

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public static AssetDailySnapshot createSnapshot(User user, LocalDate snapshotDate,
                                                     Long totalAssetAmount, Long retirementAmount,
                                                     Long investmentAmount, Long totalDebtAmount,
                                                     Long netWorthAmount) {
        AssetDailySnapshot snapshot = new AssetDailySnapshot();
        snapshot.user = user;
        snapshot.snapshotDate = snapshotDate;
        snapshot.totalAssetAmount = totalAssetAmount;
        snapshot.retirementAmount = retirementAmount;
        snapshot.investmentAmount = investmentAmount;
        snapshot.totalDebtAmount = totalDebtAmount;
        snapshot.netWorthAmount = netWorthAmount;
        return snapshot;
    }
}
