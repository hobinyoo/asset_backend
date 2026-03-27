package com.asset.asset_backend.domains.daily_report.service;

import com.asset.asset_backend.common.exception.BaseException;
import com.asset.asset_backend.common.exception.ErrorCode;
import com.asset.asset_backend.domains.auth.entity.User;
import com.asset.asset_backend.domains.auth.repository.UserRepository;
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
    private final UserRepository userRepository;

    @Transactional
    public DailyReport generateDailyReport(Long userId) {
        LocalDate today = LocalDate.now();
        return dailyReportRepository.findByReportDateAndUser_Id(today, userId)
                .orElseGet(() -> createNewReport(today, userId));
    }

    private DailyReport createNewReport(LocalDate date, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND, "User ID: " + userId));

        List<Investment> investments = investmentRepository.findByAsset_UserId(userId);

        if (investments.isEmpty()) {
            throw new RuntimeException("보유 종목이 없습니다.");
        }

        String stockInfo = buildStockInfo(investments);

        log.info("Claude API 호출 시작 - 전체 리포트");
        String fullContent = claudeApiService.generateReport(buildFullPrompt(date, stockInfo));

        log.info("Claude API 호출 시작 - 요약");
        String summaryContent = claudeApiService.generateReport(buildSummaryPrompt(date, stockInfo));

        DailyReport report = DailyReport.create(date, fullContent, summaryContent, user);
        return dailyReportRepository.save(report);
    }

    private String buildStockInfo(List<Investment> investments) {
        return investments.stream()
                .collect(Collectors.groupingBy(Investment::getCategory))
                .entrySet().stream()
                .map(entry -> {
                    String category = entry.getKey();
                    String items = entry.getValue().stream()
                            .map(inv -> String.format(
                                    "  - %s | 계좌: %s | 명의: %s | 매수단가: %s | 수량: %s",
                                    inv.getStockName(),
                                    inv.getAsset() != null ? inv.getAsset().getCategory() : "-",
                                    inv.getOwner(),
                                    inv.getPurchasePrice() != null ? inv.getPurchasePrice() + "원" : "직접입력",
                                    inv.getQuantity() != null ? inv.getQuantity() + "주" : "-"
                            ))
                            .collect(Collectors.joining("\n"));
                    return "[" + category + "]\n" + items;
                })
                .collect(Collectors.joining("\n\n"));
    }

    private String buildFullPrompt(LocalDate date, String stockInfo) {
        return String.format("""
                당신은 개인 투자 포트폴리오 분석 전문가입니다.
                오늘 날짜: %s

                아래는 현재 보유 중인 투자 종목입니다 (카테고리별 정리):
                %s

                반드시 아래 HTML 구조 그대로 작성하세요.
                다른 텍스트, 마크다운, 코드블록 없이 HTML 태그만 출력하세요.

                <section class="market-summary">
                  <h2>시장 동향</h2>
                  <p><!-- 오늘 글로벌/국내 시장 전반 동향 한 줄 요약 --></p>
                </section>

                <section class="stock-analysis">
                  <h2>카테고리별 분석</h2>
                  <!-- 카테고리마다 아래 div 반복 -->
                  <div class="stock-item">
                    <h3><!-- 카테고리명 (보유 종목: 종목1, 종목2 ...) --></h3>
                    <p class="news"><!-- 해당 카테고리 오늘 주요 이슈 --></p>
                    <p class="outlook"><!-- 오늘 기준 흐름 및 주목 포인트 --></p>
                  </div>
                </section>

                <section class="portfolio">
                  <h2>포트폴리오 분석</h2>
                  <p class="risk"><!-- 전체 포트폴리오 리스크 요인 --></p>
                  <p class="opportunity"><!-- 오늘 기준 기회 요인 --></p>
                </section>

                <section class="action">
                  <h2>오늘의 액션</h2>
                  <p><!-- 오늘 취할 구체적인 투자 액션 1가지 --></p>
                </section>

                응답은 반드시 3000토큰 이내로 작성하세요.
                """, date, stockInfo);
    }

    private String buildSummaryPrompt(LocalDate date, String stockInfo) {
        return String.format("""
                당신은 개인 투자 포트폴리오 분석 전문가입니다.
                오늘 날짜: %s

                아래는 현재 보유 중인 투자 종목입니다 (카테고리별 정리):
                %s

                반드시 아래 형식 그대로만 출력하세요. 다른 텍스트는 절대 추가하지 마세요.

                📈 {날짜} 데일리 브리핑
                시장: {한 줄 시장 동향}
                주목: {오늘 가장 주목할 카테고리 또는 이슈}
                액션: {오늘 취할 액션 한 줄}
                """, date, stockInfo);
    }

    public List<DailyReport> getAllReports(Long userId) {
        return dailyReportRepository.findAllByUser_IdOrderByReportDateDesc(userId);
    }

    public DailyReport getReportByDate(LocalDate date, Long userId) {
        return dailyReportRepository.findByReportDateAndUser_Id(date, userId)
                .orElseThrow(() -> new RuntimeException("해당 날짜의 리포트가 없습니다: " + date));
    }
}