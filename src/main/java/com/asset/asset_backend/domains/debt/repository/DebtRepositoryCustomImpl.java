package com.asset.asset_backend.domains.debt.repository;

import com.asset.asset_backend.common.enums.AssetType;
import com.asset.asset_backend.domains.debt.entity.Debt;
import com.asset.asset_backend.domains.debt.entity.QDebt;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RequiredArgsConstructor
public class DebtRepositoryCustomImpl implements DebtRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Debt> searchDebts(String category, String owner, AssetType type, Pageable pageable) {
        QDebt debt = QDebt.debt;

        List<Debt> debts = queryFactory
                .selectFrom(debt)
                .where(
                        containsCategory(category),
                        eqOwner(owner),
                        eqType(type)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(debt.count())
                .from(debt)
                .where(
                        containsCategory(category),
                        eqOwner(owner),
                        eqType(type)
                )
                .fetchOne();

        return new PageImpl<>(debts, pageable, total != null ? total : 0);
    }

    private BooleanExpression containsCategory(String category) {
        return category != null ? QDebt.debt.category.containsIgnoreCase(category) : null;
    }

    private BooleanExpression eqOwner(String owner) {
        return owner != null ? QDebt.debt.owner.eq(owner) : null;
    }

    private BooleanExpression eqType(AssetType type) {
        return type != null ? QDebt.debt.type.eq(type) : null;
    }
}