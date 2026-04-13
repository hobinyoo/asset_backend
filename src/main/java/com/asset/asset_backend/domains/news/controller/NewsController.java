package com.asset.asset_backend.domains.news.controller;

import com.asset.asset_backend.common.response.ApiResult;
import com.asset.asset_backend.domains.news.service.NewsCollectorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsCollectorService newsCollectorService;

    @PostMapping("/collect")
    public ResponseEntity<ApiResult<Integer>> collect(
            @AuthenticationPrincipal Long userId) {
        int count = newsCollectorService.collect(userId);
        return ResponseEntity.ok(ApiResult.success(count, "뉴스 수집이 완료되었습니다."));
    }

    @PostMapping("/embed")
    public ResponseEntity<ApiResult<Integer>> embed(
            @AuthenticationPrincipal Long userId) {
        int count = newsCollectorService.embedAll();
        return ResponseEntity.ok(ApiResult.success(count, "임베딩이 완료되었습니다."));
    }
}