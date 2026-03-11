package com.asset.asset_backend.domains.debt.service;


import com.asset.asset_backend.common.enums.AssetType;
import com.asset.asset_backend.common.exception.BaseException;
import com.asset.asset_backend.common.exception.ErrorCode;
import com.asset.asset_backend.domains.debt.dto.request.DebtCreateRequest;
import com.asset.asset_backend.domains.debt.dto.request.DebtUpdateRequest;
import com.asset.asset_backend.domains.debt.dto.response.DebtSummaryResponse;
import com.asset.asset_backend.domains.debt.entity.Debt;
import com.asset.asset_backend.domains.debt.repository.DebtRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DebtService {

    private final DebtRepository debtRepository;

    @Transactional
    public Debt createDebt(DebtCreateRequest request) {
        Debt debt = Debt.createDebt(
                request.getCategory(),
                request.getOwner(),
                request.getAmount(),
                request.getType(),
                request.getMonthlyPayment(),
                request.getPaymentDay(),
                request.getPurpose(),
                request.getNote()
        );
        return debtRepository.save(debt);
    }

    public Page<Debt> searchDebts(String category, String owner, AssetType type, Pageable pageable) {
        return debtRepository.searchDebts(category, owner, type, pageable);
    }

    public Debt getDebtById(Long id) {
        return debtRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Debt ID: " + id));
    }

    @Transactional
    public Debt updateDebt(Long id, DebtUpdateRequest request) {
        Debt debt = getDebtById(id);
        debt.updateDebtInfo(
                request.getCategory(),
                request.getOwner(),
                request.getAmount(),
                request.getType(),
                request.getMonthlyPayment(),
                request.getPaymentDay(),
                request.getPurpose(),
                request.getNote()
        );
        return debt;
    }

    @Transactional
    public void deleteDebt(Long id) {
        Debt debt = getDebtById(id);
        debtRepository.delete(debt);
    }

    public DebtSummaryResponse getSummary() {
        Long totalAmount = debtRepository.sumAllAmount();
        Long totalMonthlyPayment = debtRepository.sumAllMonthlyPayment();
        return DebtSummaryResponse.from(totalAmount, totalMonthlyPayment);
    }
}