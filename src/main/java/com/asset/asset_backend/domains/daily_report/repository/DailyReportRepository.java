package com.asset.asset_backend.domains.daily_report.repository;

import com.asset.asset_backend.domains.daily_report.entity.DailyReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyReportRepository extends JpaRepository<DailyReport, Long> {
    Optional<DailyReport> findByReportDate(LocalDate reportDate);
    List<DailyReport> findAllByOrderByReportDateDesc();  // 최신순 전체 조회
}