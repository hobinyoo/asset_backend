package com.asset.asset_backend.domains.investment.service;

import com.asset.asset_backend.common.enums.AssetType;
import com.asset.asset_backend.common.enums.MarketType;
import com.asset.asset_backend.domains.investment.dto.investment.CategoryAmountDto;
import com.asset.asset_backend.domains.investment.dto.response.InvestmentDashboardChartResponse;
import com.asset.asset_backend.domains.investment.dto.response.InvestmentDashboardSummaryResponse;
import com.asset.asset_backend.domains.investment.entity.Investment;
import com.asset.asset_backend.domains.investment.repository.InvestmentRepository;
import com.asset.asset_backend.domains.snapshot.entity.InvestmentCategorySnapshot;
import com.asset.asset_backend.domains.snapshot.repository.AssetDailySnapshotRepository;
import com.asset.asset_backend.domains.snapshot.repository.InvestmentCategorySnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvestmentDashboardService {

    private final InvestmentRepository investmentRepository;
    private final AssetDailySnapshotRepository snapshotRepository;
    private final InvestmentCategorySnapshotRepository categorySnapshotRepository;
    private final StockPriceService stockPriceService;
    private final ExchangeRateService exchangeRateService;

    public InvestmentDashboardSummaryResponse getSummary(Long userId) {
        Map<String, Long> categoryAmounts = getInvestmentCategoryAmounts(userId);
        long totalAmount = categoryAmounts.values().stream().mapToLong(Long::longValue).sum();

        List<InvestmentDashboardSummaryResponse.CategorySummary> categories = categoryAmounts.entrySet().stream()
                .map(e -> {
                    double percentage = totalAmount > 0
                            ? Math.round(e.getValue() * 1000.0 / totalAmount) / 10.0
                            : 0.0;
                    return InvestmentDashboardSummaryResponse.CategorySummary.of(e.getKey(), e.getValue(), percentage);
                })
                .toList();

        return InvestmentDashboardSummaryResponse.from(totalAmount, categories);
    }

    public InvestmentDashboardChartResponse getChart(Long userId, String period) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = switch (period) {
            case "7d"  -> today.minusDays(7);
            case "90d" -> today.minusDays(90);
            case "1y"  -> today.minusYears(1);
            default    -> today.minusDays(30); // "30d" 기본
        };

        // 날짜별 카테고리 스냅샷을 한 번에 조회 (N+1 방지)
        List<InvestmentCategorySnapshot> allCategorySnapshots = categorySnapshotRepository
                .findByUser_IdAndSnapshotDateBetweenOrderBySnapshotDateAsc(userId, startDate, today);

        Map<LocalDate, List<CategoryAmountDto>> categoryByDate = allCategorySnapshots.stream()
                .collect(Collectors.groupingBy(
                        InvestmentCategorySnapshot::getSnapshotDate,
                        Collectors.mapping(
                                cs -> CategoryAmountDto.of(cs.getCategory(), cs.getAmount()),
                                Collectors.toList()
                        )
                ));

        // AssetDailySnapshot 날짜 기준으로 데이터 포인트 구성
        List<InvestmentDashboardChartResponse.ChartDataPoint> data = snapshotRepository
                .findByUser_IdAndSnapshotDateBetweenOrderBySnapshotDateAsc(userId, startDate, today)
                .stream()
                .map(snapshot -> InvestmentDashboardChartResponse.ChartDataPoint.of(
                        snapshot.getSnapshotDate(),
                        categoryByDate.getOrDefault(snapshot.getSnapshotDate(), List.of())
                ))
                .toList();

        return InvestmentDashboardChartResponse.from(period, data);
    }

    // AssetScheduler에서도 호출
    public Map<String, Long> getInvestmentCategoryAmounts(Long userId) {
        List<Investment> investments = investmentRepository.findByAsset_TypeAndAsset_User_Id(AssetType.INVESTMENT, userId);

        boolean hasOverseas = investments.stream().anyMatch(inv -> inv.getMarketType() == MarketType.OVERSEAS);
        Double exchangeRate = hasOverseas ? exchangeRateService.getUsdToKrw() : null;

        Map<String, Long> categoryAmounts = new LinkedHashMap<>();
        for (Investment inv : investments) {
            long value = resolveCurrentValue(inv, exchangeRate);
            categoryAmounts.merge(inv.getCategory(), value, Long::sum);
        }
        return categoryAmounts;
    }

    private long resolveCurrentValue(Investment inv, Double exchangeRate) {
        Long currentPrice = stockPriceService.getCurrentPrice(inv.getTicker());
        if (currentPrice == null || inv.getQuantity() == null) {
            return inv.getPurchaseAmount() != null ? inv.getPurchaseAmount() : 0L;
        }
        if (inv.getMarketType() == MarketType.OVERSEAS && exchangeRate != null) {
            return Math.round(currentPrice * exchangeRate) * inv.getQuantity();
        }
        return currentPrice * inv.getQuantity();
    }
}
