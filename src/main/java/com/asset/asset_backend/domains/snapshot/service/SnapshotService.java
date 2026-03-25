package com.asset.asset_backend.domains.snapshot.service;

import com.asset.asset_backend.common.exception.BaseException;
import com.asset.asset_backend.common.exception.ErrorCode;
import com.asset.asset_backend.domains.snapshot.entity.AssetDailySnapshot;
import com.asset.asset_backend.domains.snapshot.repository.AssetDailySnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SnapshotService {

    private final AssetDailySnapshotRepository snapshotRepository;

    public List<AssetDailySnapshot> getSnapshots(Long userId, String period) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = resolveStartDate(period, endDate);
        return snapshotRepository.findByUser_IdAndSnapshotDateBetweenOrderBySnapshotDateAsc(
                userId, startDate, endDate);
    }

    private LocalDate resolveStartDate(String period, LocalDate endDate) {
        return switch (period) {
            case "7d"  -> endDate.minusDays(7);
            case "30d" -> endDate.minusDays(30);
            case "90d" -> endDate.minusDays(90);
            case "1y"  -> endDate.minusYears(1);
            default    -> throw new BaseException(ErrorCode.INVALID_INPUT,
                    "유효하지 않은 period 값입니다. 허용값: 7d, 30d, 90d, 1y");
        };
    }
}
