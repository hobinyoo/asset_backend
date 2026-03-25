package com.asset.asset_backend.domains.asset.scheduler;

import com.asset.asset_backend.common.enums.AssetType;
import com.asset.asset_backend.domains.asset.entity.Asset;
import com.asset.asset_backend.domains.asset.repository.AssetRepository;
import com.asset.asset_backend.domains.auth.entity.User;
import com.asset.asset_backend.domains.auth.repository.UserRepository;
import com.asset.asset_backend.domains.debt.repository.DebtRepository;
import com.asset.asset_backend.domains.investment.service.InvestmentService;
import com.asset.asset_backend.domains.snapshot.entity.AssetDailySnapshot;
import com.asset.asset_backend.domains.snapshot.repository.AssetDailySnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AssetScheduler {

    private final AssetRepository assetRepository;
    private final AssetDailySnapshotRepository snapshotRepository;
    private final DebtRepository debtRepository;
    private final UserRepository userRepository;
    private final InvestmentService investmentService;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void saveDailySnapshot() {
        log.info("[AssetScheduler] 일일 스냅샷 저장 시작");
        List<User> users = userRepository.findAll();
        for (User user : users) {
            processSnapshotForUser(user);
        }
        log.info("[AssetScheduler] 일일 스냅샷 저장 완료 - {}명 처리", users.size());
    }

    private void processSnapshotForUser(User user) {
        Long userId = user.getId();

        List<Asset> linkedAssets = assetRepository.findByLinkedToInvestmentTrueAndUser_Id(userId);
        for (Asset asset : linkedAssets) {
            investmentService.syncAssetAmount(asset.getId());
        }

        Long totalAssetAmount = assetRepository.sumAllAmountByMemberId(userId);
        Long retirementAmount = assetRepository.sumAmountByTypeAndMemberId(AssetType.RETIREMENT, userId);
        Long investmentAmount = assetRepository.sumAmountByTypeAndMemberId(AssetType.INVESTMENT, userId);
        Long totalDebtAmount = debtRepository.sumAllAmountByMemberId(userId);
        Long netWorthAmount = totalAssetAmount - totalDebtAmount;

        AssetDailySnapshot snapshot = AssetDailySnapshot.createSnapshot(
                user, LocalDate.now(),
                totalAssetAmount, retirementAmount, investmentAmount,
                totalDebtAmount, netWorthAmount
        );
        snapshotRepository.save(snapshot);

        log.info("[AssetScheduler] 스냅샷 저장 - userId={}, 총자산={}원, 총부채={}원, 순자산={}원",
                userId, totalAssetAmount, totalDebtAmount, netWorthAmount);
    }
}
