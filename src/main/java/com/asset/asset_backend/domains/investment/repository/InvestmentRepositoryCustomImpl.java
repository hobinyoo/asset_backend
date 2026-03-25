package com.asset.asset_backend.domains.investment.repository;

import com.asset.asset_backend.domains.investment.entity.Investment;
import com.asset.asset_backend.domains.investment.entity.QInvestment;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RequiredArgsConstructor
public class InvestmentRepositoryCustomImpl implements InvestmentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Investment> searchInvestments(String owner, String category, Long assetId, Long userId, Pageable pageable) {
        QInvestment investment = QInvestment.investment;

        List<Investment> investments = queryFactory
                .selectFrom(investment)
                .where(
                        investment.asset.user.id.eq(userId),
                        eqOwner(owner),
                        containsCategory(category),
                        eqAssetId(assetId)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(investment.count())
                .from(investment)
                .where(
                        investment.asset.user.id.eq(userId),
                        eqOwner(owner),
                        containsCategory(category),
                        eqAssetId(assetId)
                )
                .fetchOne();

        return new PageImpl<>(investments, pageable, total != null ? total : 0);
    }

    private BooleanExpression eqAssetId(Long assetId) {
        return assetId != null ? QInvestment.investment.asset.id.eq(assetId) : null;
    }

    private BooleanExpression eqOwner(String owner) {
        return owner != null ? QInvestment.investment.owner.eq(owner) : null;
    }

    private BooleanExpression containsCategory(String category) {
        return category != null ? QInvestment.investment.category.containsIgnoreCase(category) : null;
    }
}
