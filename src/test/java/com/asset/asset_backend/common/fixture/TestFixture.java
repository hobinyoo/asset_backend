package com.asset.asset_backend.common.fixture;

import com.asset.asset_backend.common.enums.AssetType;
import com.asset.asset_backend.common.enums.MarketType;
import com.asset.asset_backend.domains.asset.entity.Asset;
import com.asset.asset_backend.domains.auth.entity.Member;
import com.asset.asset_backend.domains.auth.entity.RefreshToken;
import com.asset.asset_backend.domains.config.entity.ConfigType;
import com.asset.asset_backend.domains.config.entity.MemberConfig;
import com.asset.asset_backend.domains.debt.entity.Debt;
import com.asset.asset_backend.domains.investment.entity.Investment;
import com.asset.asset_backend.domains.snapshot.entity.AssetDailySnapshot;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TestFixture {

    public static final String MEMBER_A_ID = "memberA";
    public static final String MEMBER_B_ID = "memberB";
    public static final String RAW_PASSWORD = "password123";

    public static Member createMember(String loginId, String encodedPassword) {
        return Member.createMember(loginId, encodedPassword);
    }

    public static Asset createAsset(Member member) {
        return Asset.createAsset("청약저축", "본인", 10_000_000L,
                AssetType.SAVINGS, 100_000L, 15, null, false, 1, member);
    }

    public static Asset createLinkedAsset(Member member) {
        return Asset.createAsset("KB증권", "본인", 5_000_000L,
                AssetType.INVESTMENT, null, null, null, true, 2, member);
    }

    public static Asset createAsset(Member member, int sortOrder) {
        return Asset.createAsset("자산" + sortOrder, "본인", 1_000_000L,
                AssetType.SAVINGS, null, null, null, false, sortOrder, member);
    }

    public static Debt createDebt(Member member) {
        return Debt.createDebt("신용대출", "본인", 5_000_000L,
                AssetType.SAVINGS, 100_000L, 15, "생활비", null, member);
    }

    public static Investment createInvestment(Asset asset) {
        return Investment.createInvestment(asset, "ETF", "S&P500", "SPY",
                "본인", 50_000L, 10L, 500_000L, MarketType.OVERSEAS);
    }

    public static AssetDailySnapshot createSnapshot(Member member, LocalDate date) {
        return AssetDailySnapshot.createSnapshot(member, date,
                10_000_000L, 2_000_000L, 3_000_000L, 5_000_000L, 5_000_000L);
    }

    public static MemberConfig createMemberConfig(Member member, ConfigType configType, String value) {
        return MemberConfig.create(member, configType, value);
    }

    public static RefreshToken createRefreshToken(Member member, String token) {
        return RefreshToken.createRefreshToken(member, token,
                LocalDateTime.now().plusDays(7));
    }
}
