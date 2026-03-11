package com.asset.asset_backend.domains.daily_report.service;

import com.asset.asset_backend.domains.daily_report.entity.DailyReport;
import com.asset.asset_backend.domains.daily_report.repository.DailyReportRepository;
import com.asset.asset_backend.domains.investment.entity.Investment;
import com.asset.asset_backend.domains.investment.repository.InvestmentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyReportService {

    private final InvestmentRepository investmentRepository;
    private final DailyReportRepository dailyReportRepository;
    private final ClaudeApiService claudeApiService;

    @Transactional
    public DailyReport generateDailyReport() {
        LocalDate today = LocalDate.now();
        return dailyReportRepository.findByReportDate(today)
                .orElseGet(() -> createNewReport(today));
    }

    private DailyReport createNewReport(LocalDate date) {
        List<Investment> investments = investmentRepository.findAll();

        if (investments.isEmpty()) {
            throw new RuntimeException("보유 종목이 없습니다.");
        }

        String stockInfo = investments.stream()
                .map(inv -> String.format(
                        "- %s | 계좌: %s | 카테고리: %s | 명의: %s | 매수단가: %s | 수량: %s",
                        inv.getStockName(),
                        inv.getAsset() != null ? inv.getAsset().getCategory() : "-",
                        inv.getCategory(),
                        inv.getOwner(),
                        inv.getPurchasePrice() != null ? inv.getPurchasePrice() + "원" : "직접입력",
                        inv.getQuantity() != null ? inv.getQuantity() + "주" : "-"
                ))
                .collect(Collectors.joining("\n"));

        // 전체 리포트 - HTML 구조 고정
        String fullPrompt = String.format("""
                당신은 개인 투자 포트폴리오 분석 전문가입니다.
                오늘 날짜: %s
                
                아래는 현재 보유 중인 투자 종목입니다:
                %s
                
                반드시 아래 HTML 구조 그대로 작성하세요.
                다른 텍스트, 마크다운, 코드블록 없이 HTML 태그만 출력하세요.
                
                <section class="market-summary">
                  <h2>시장 동향</h2>
                  <p><!-- 오늘 글로벌/국내 시장 전반 동향 요약 --></p>
                </section>
                
                <section class="stock-analysis">
                  <h2>종목별 분석</h2>
                  <!-- 보유 종목마다 아래 div 반복 -->
                  <div class="stock-item">
                    <h3><!-- 종목명 --></h3>
                    <p class="news"><!-- 최근 뉴스 및 이슈 --></p>
                    <p class="outlook"><!-- 단기 전망 --></p>
                  </div>
                </section>
                
                <section class="portfolio">
                  <h2>포트폴리오 분석</h2>
                  <p class="risk"><!-- 리스크 요인 --></p>
                  <p class="opportunity"><!-- 기회 요인 --></p>
                </section>
                
                <section class="action">
                  <h2>오늘의 액션</h2>
                  <p><!-- 구체적인 투자 액션 제안 --></p>
                </section>
                
                응답은 반드시 3000토큰 이내로 작성하세요.
                """, date, stockInfo);

        // 카톡 요약 - 포맷 고정
        String summaryPrompt = String.format("""
                당신은 개인 투자 포트폴리오 분석 전문가입니다.
                오늘 날짜: %s
                
                아래는 현재 보유 중인 투자 종목입니다:
                %s
                
                반드시 아래 형식 그대로만 출력하세요. 다른 텍스트는 절대 추가하지 마세요.
                
                📈 {날짜} 데일리 브리핑
                시장: {한 줄 시장 동향}
                주목: {오늘 가장 주목할 종목 또는 이슈}
                액션: {오늘 취할 액션 한 줄}
                """, date, stockInfo);

        log.info("Claude API 호출 시작 - 전체 리포트");
        String fullContent = claudeApiService.generateReport(fullPrompt);

        log.info("Claude API 호출 시작 - 요약");
        String summaryContent = claudeApiService.generateReport(summaryPrompt);

        DailyReport report = DailyReport.create(date, fullContent, summaryContent);
        return dailyReportRepository.save(report);
    }

    public List<DailyReport> getAllReports() {
        return dailyReportRepository.findAllByOrderByReportDateDesc();
    }

    public DailyReport getReportByDate(LocalDate date) {
        return dailyReportRepository.findByReportDate(date)
                .orElseThrow(() -> new RuntimeException("해당 날짜의 리포트가 없습니다: " + date));
    }
}