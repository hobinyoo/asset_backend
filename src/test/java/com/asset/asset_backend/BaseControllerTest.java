package com.asset.asset_backend;

import com.asset.asset_backend.domains.asset.repository.AssetRepository;
import com.asset.asset_backend.domains.asset.scheduler.AssetPaymentScheduler;
import com.asset.asset_backend.domains.asset.scheduler.AssetScheduler;
import com.asset.asset_backend.domains.auth.jwt.JwtProvider;
import com.asset.asset_backend.domains.auth.repository.RefreshTokenRepository;
import com.asset.asset_backend.domains.config.repository.MemberConfigRepository;
import com.asset.asset_backend.domains.daily_report.repository.DailyReportRepository;
import com.asset.asset_backend.domains.daily_report.service.ClaudeApiService;
import com.asset.asset_backend.domains.debt.repository.DebtRepository;
import com.asset.asset_backend.domains.debt.scheduler.DebtPaymentScheduler;
import com.asset.asset_backend.domains.investment.repository.InvestmentRepository;
import com.asset.asset_backend.domains.investment.service.ExchangeRateService;
import com.asset.asset_backend.domains.investment.service.StockPriceService;
import com.asset.asset_backend.domains.snapshot.repository.AssetDailySnapshotRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class BaseControllerTest {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected JwtProvider jwtProvider;
    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected PasswordEncoder passwordEncoder;

    // repositories for cleanup
    @Autowired protected MemberRepository memberRepository;
    @Autowired protected RefreshTokenRepository refreshTokenRepository;
    @Autowired protected AssetRepository assetRepository;
    @Autowired protected DebtRepository debtRepository;
    @Autowired protected InvestmentRepository investmentRepository;
    @Autowired protected AssetDailySnapshotRepository snapshotRepository;
    @Autowired protected DailyReportRepository dailyReportRepository;
    @Autowired protected MemberConfigRepository memberConfigRepository;

    // mock external services
    @MockBean protected StockPriceService stockPriceService;
    @MockBean protected ExchangeRateService exchangeRateService;
    @MockBean protected ClaudeApiService claudeApiService;

    // mock schedulers (prevent @Scheduled from running)
    @MockBean protected AssetScheduler assetScheduler;
    @MockBean protected AssetPaymentScheduler assetPaymentScheduler;
    @MockBean protected DebtPaymentScheduler debtPaymentScheduler;

    @AfterEach
    void cleanUp() {
        // FK 제약 순서에 맞게 삭제
        investmentRepository.deleteAll();
        snapshotRepository.deleteAll();
        dailyReportRepository.deleteAll();
        memberConfigRepository.deleteAll();
        assetRepository.deleteAll();
        debtRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        memberRepository.deleteAll();
    }

    protected Cookie authCookie(String loginId) {
        return new Cookie("access_token", jwtProvider.generateAccessToken(loginId));
    }
}
