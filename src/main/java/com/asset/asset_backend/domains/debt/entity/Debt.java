package com.asset.asset_backend.domains.debt.entity;

import com.asset.asset_backend.common.enums.AssetType;
import com.asset.asset_backend.domains.auth.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "debt")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Debt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String category;        // 신용대출, 주택청약대출 등

    @Column(nullable = false)
    private String owner;           // 호빈, 선주, 공동

    @Column(nullable = false)
    private Long amount;            // 잔액

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetType type;          // FIXED, REGULAR, VARIABLE

    private Long monthlyPayment;    // 상환액(월), 없으면 null

    private Integer paymentDay;     // 월 상환일 (1~31), 없으면 null

    private String purpose;         // 대출목적 (집보증금 등)

    private String note;            // 비고

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public static Debt createDebt(String category, String owner, Long amount,
                                  AssetType type, Long monthlyPayment, Integer paymentDay,
                                  String purpose, String note, User user) {
        Debt debt = new Debt();
        debt.category = category;
        debt.owner = owner;
        debt.amount = amount;
        debt.type = type;
        debt.monthlyPayment = monthlyPayment;
        debt.paymentDay = paymentDay;
        debt.purpose = purpose;
        debt.note = note;
        debt.user = user;
        return debt;
    }

    public void updateDebtInfo(String category, String owner, Long amount,
                               AssetType type, Long monthlyPayment, Integer paymentDay,
                               String purpose, String note) {
        if (category != null) this.category = category;
        if (owner != null) this.owner = owner;
        if (amount != null) this.amount = amount;
        if (type != null) this.type = type;
        if (monthlyPayment != null) this.monthlyPayment = monthlyPayment;
        if (paymentDay != null) this.paymentDay = paymentDay;
        if (purpose != null) this.purpose = purpose;
        if (note != null) this.note = note;
    }

    public void repayMonthly() {
        if (this.monthlyPayment != null && this.amount > 0) {
            this.amount = Math.max(0, this.amount - this.monthlyPayment);
        }
    }
}