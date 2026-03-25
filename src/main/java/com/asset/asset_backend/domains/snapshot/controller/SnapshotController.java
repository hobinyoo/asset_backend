package com.asset.asset_backend.domains.snapshot.controller;

import com.asset.asset_backend.common.response.ApiResult;
import com.asset.asset_backend.domains.snapshot.dto.response.SnapshotResponse;
import com.asset.asset_backend.domains.snapshot.service.SnapshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/snapshots")
@RequiredArgsConstructor
public class SnapshotController {

    private final SnapshotService snapshotService;

    @GetMapping
    public ResponseEntity<ApiResult<List<SnapshotResponse>>> getSnapshots(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "30d") String period) {
        List<SnapshotResponse> responses = snapshotService.getSnapshots(userId, period).stream()
                .map(SnapshotResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResult.success(responses, "스냅샷 목록을 조회했습니다."));
    }
}
