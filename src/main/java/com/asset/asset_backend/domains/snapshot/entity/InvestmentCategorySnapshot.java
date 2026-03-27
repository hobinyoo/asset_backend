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
@Table(name = "investment_category_snapshot")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InvestmentCategorySnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate snapshotDate;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false)
    private Long amount;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public static InvestmentCategorySnapshot create(User user, LocalDate snapshotDate, String category, Long amount) {
        InvestmentCategorySnapshot snapshot = new InvestmentCategorySnapshot();
        snapshot.user = user;
        snapshot.snapshotDate = snapshotDate;
        snapshot.category = category;
        snapshot.amount = amount;
        return snapshot;
    }

    public void updateAmount(Long amount) {
        this.amount = amount;
    }
}
