package com.asset.asset_backend.domains.daily_report.entity;

import com.asset.asset_backend.domains.auth.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_report")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate reportDate;       // 리포트 날짜

    @Column(columnDefinition = "TEXT", nullable = false)
    private String fullContent;         // 전체 리포트 (프론트용)

    @Column(columnDefinition = "TEXT", nullable = false)
    private String summaryContent;      // 요약본 (카톡용)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public static DailyReport create(LocalDate reportDate, String fullContent, String summaryContent, User user) {
        DailyReport report = new DailyReport();
        report.reportDate = reportDate;
        report.fullContent = fullContent;
        report.summaryContent = summaryContent;
        report.user = user;
        return report;
    }
}