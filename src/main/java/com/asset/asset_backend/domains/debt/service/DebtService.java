package com.asset.asset_backend.domains.debt.service;

import com.asset.asset_backend.common.enums.AssetType;
import com.asset.asset_backend.common.exception.BaseException;
import com.asset.asset_backend.common.exception.ErrorCode;
import com.asset.asset_backend.domains.auth.entity.User;
import com.asset.asset_backend.domains.auth.repository.UserRepository;
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
    private final UserRepository userRepository;

    @Transactional
    public Debt createDebt(DebtCreateRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND, "User ID: " + userId));
        Debt debt = Debt.createDebt(
                request.getCategory(),
                request.getOwner(),
                request.getAmount(),
                request.getType(),
                request.getMonthlyPayment(),
                request.getPaymentDay(),
                request.getPurpose(),
                request.getNote(),
                user
        );
        return debtRepository.save(debt);
    }

    public Page<Debt> searchDebts(String category, String owner, AssetType type, Long userId, Pageable pageable) {
        return debtRepository.searchDebts(category, owner, type, userId, pageable);
    }

    public Debt getDebtById(Long id, Long userId) {
        Debt debt = debtRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Debt ID: " + id));
        validateOwnership(debt.getUser().getId(), userId);
        return debt;
    }

    @Transactional
    public Debt updateDebt(Long id, DebtUpdateRequest request, Long userId) {
        Debt debt = getDebtById(id, userId);
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
    public void deleteDebt(Long id, Long userId) {
        Debt debt = getDebtById(id, userId);
        debtRepository.delete(debt);
    }

    public DebtSummaryResponse getSummary(Long userId) {
        Long totalAmount = debtRepository.sumAllAmountByMemberId(userId);
        Long totalMonthlyPayment = debtRepository.sumAllMonthlyPaymentByMemberId(userId);
        return DebtSummaryResponse.from(totalAmount, totalMonthlyPayment);
    }

    private void validateOwnership(Long debtUserId, Long userId) {
        if (!debtUserId.equals(userId)) {
            throw new BaseException(ErrorCode.FORBIDDEN, "해당 부채에 접근 권한이 없습니다");
        }
    }
}
