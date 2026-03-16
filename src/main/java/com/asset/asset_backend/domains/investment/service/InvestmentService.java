package com.asset.asset_backend.domains.investment.service;

import com.asset.asset_backend.common.enums.MarketType;
import com.asset.asset_backend.common.exception.BaseException;
import com.asset.asset_backend.common.exception.ErrorCode;
import com.asset.asset_backend.domains.asset.entity.Asset;
import com.asset.asset_backend.domains.asset.repository.AssetRepository;
import com.asset.asset_backend.domains.investment.dto.request.InvestmentCreateRequest;
import com.asset.asset_backend.domains.investment.dto.request.InvestmentUpdateRequest;
import com.asset.asset_backend.domains.investment.dto.response.InvestmentResponse;
import com.asset.asset_backend.domains.investment.entity.Investment;
import com.asset.asset_backend.domains.investment.repository.InvestmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvestmentService {

    private final InvestmentRepository investmentRepository;
    private final AssetRepository assetRepository;
    private final StockPriceService stockPriceService;
    private final ExchangeRateService exchangeRateService;

    @Transactional
    public InvestmentResponse createInvestment(InvestmentCreateRequest request) {
        Asset asset = assetRepository.findById(request.getAssetId())
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Asset ID: " + request.getAssetId()));

        Investment investment = Investment.createInvestment(
                asset,
                request.getCategory(),
                request.getStockName(),
                request.getTicker(),
                request.getOwner(),
                request.getPurchasePrice(),
                request.getQuantity(),
                request.getPurchaseAmount(),
                request.getMarketType()
        );
        Investment saved = investmentRepository.save(investment);
        Long currentPrice = stockPriceService.getCurrentPrice(saved.getTicker());
        Double exchangeRate = saved.getMarketType() == MarketType.OVERSEAS ? exchangeRateService.getUsdToKrw() : null;
        return InvestmentResponse.from(saved, currentPrice, exchangeRate);
    }

    public Page<InvestmentResponse> searchInvestments(String owner, String category, Long assetId, Pageable pageable) {
        Page<Investment> investmentPage = investmentRepository.searchInvestments(owner, category, assetId, pageable);

        boolean hasOverseas = investmentPage.getContent().stream()
                .anyMatch(inv -> inv.getMarketType() == MarketType.OVERSEAS);
        Double exchangeRate = hasOverseas ? exchangeRateService.getUsdToKrw() : null;

        List<InvestmentResponse> responses = investmentPage.getContent().stream()
                .map(inv -> InvestmentResponse.from(inv, stockPriceService.getCurrentPrice(inv.getTicker()), exchangeRate))
                .toList();

        return new PageImpl<>(responses, pageable, investmentPage.getTotalElements());
    }

    public InvestmentResponse getInvestmentById(Long id) {
        Investment investment = investmentRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Investment ID: " + id));
        Long currentPrice = stockPriceService.getCurrentPrice(investment.getTicker());
        Double exchangeRate = investment.getMarketType() == MarketType.OVERSEAS ? exchangeRateService.getUsdToKrw() : null;
        return InvestmentResponse.from(investment, currentPrice, exchangeRate);
    }

    public List<InvestmentResponse> getInvestmentsByAssetId(Long assetId) {
        List<Investment> investments = investmentRepository.findByAssetId(assetId);

        boolean hasOverseas = investments.stream()
                .anyMatch(inv -> inv.getMarketType() == MarketType.OVERSEAS);
        Double exchangeRate = hasOverseas ? exchangeRateService.getUsdToKrw() : null;

        return investments.stream()
                .map(inv -> InvestmentResponse.from(inv, stockPriceService.getCurrentPrice(inv.getTicker()), exchangeRate))
                .toList();
    }

    @Transactional
    public InvestmentResponse updateInvestment(Long id, InvestmentUpdateRequest request) {
        Investment investment = investmentRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Investment ID: " + id));

        Asset asset = null;
        if (request.getAssetId() != null) {
            asset = assetRepository.findById(request.getAssetId())
                    .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Asset ID: " + request.getAssetId()));
        }

        investment.updateInvestmentInfo(
                asset,
                request.getCategory(),
                request.getStockName(),
                request.getTicker(),
                request.getOwner(),
                request.getPurchasePrice(),
                request.getQuantity(),
                request.getPurchaseAmount(),
                request.getMarketType()
        );

        Long currentPrice = stockPriceService.getCurrentPrice(investment.getTicker());
        Double exchangeRate = investment.getMarketType() == MarketType.OVERSEAS ? exchangeRateService.getUsdToKrw() : null;
        return InvestmentResponse.from(investment, currentPrice, exchangeRate);
    }

    @Transactional
    public void deleteInvestment(Long id) {
        Investment investment = investmentRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Investment ID: " + id));
        investmentRepository.delete(investment);
    }

    @Transactional
    public void syncAssetAmount(Long assetId) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Asset ID: " + assetId));

        boolean hasOverseas = investmentRepository.findByAssetId(assetId).stream()
                .anyMatch(inv -> inv.getMarketType() == MarketType.OVERSEAS);
        Double exchangeRate = hasOverseas ? exchangeRateService.getUsdToKrw() : null;

        Long totalAmount = investmentRepository.findByAssetId(assetId).stream()
                .mapToLong(inv -> {
                    Long currentPrice = stockPriceService.getCurrentPrice(inv.getTicker());
                    if (currentPrice == null || inv.getQuantity() == null) {
                        return inv.getPurchaseAmount() != null ? inv.getPurchaseAmount() : 0L;
                    }
                    if (inv.getMarketType() == MarketType.OVERSEAS && exchangeRate != null) {
                        return Math.round(currentPrice * exchangeRate) * inv.getQuantity();
                    }
                    return currentPrice * inv.getQuantity();
                })
                .sum();

        asset.updateAmount(totalAmount);
    }
}
