package com.asset.asset_backend.domains.news.scheduler;

import com.asset.asset_backend.domains.news.service.NewsCollectorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NewsScheduler {

    private final NewsCollectorService newsCollectorService;

    // 매일 오전 8시 뉴스 수집
//    @Scheduled(cron = "0 0 8 * * *")
//    public void collectNews() {
//        log.info("[NewsScheduler] 뉴스 수집 시작");
//        try {
//            newsCollectorService.collectFromNaver("주식 투자", null);
//        } catch (Exception e) {
//            log.error("[NewsScheduler] 뉴스 수집 실패: {}", e.getMessage());
//        }
//        log.info("[NewsScheduler] 뉴스 수집 완료");
//    }
//
//    // 매일 오전 9시 임베딩 처리
//    @Scheduled(cron = "0 0 9 * * *")
//    public void embedNews() {
//        log.info("[NewsScheduler] 임베딩 처리 시작");
//        try {
//            newsCollectorService.embedUnprocessed();
//        } catch (Exception e) {
//            log.error("[NewsScheduler] 임베딩 처리 실패: {}", e.getMessage());
//        }
//        log.info("[NewsScheduler] 임베딩 처리 완료");
//    }
}
