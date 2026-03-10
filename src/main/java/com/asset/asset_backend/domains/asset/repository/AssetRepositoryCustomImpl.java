package com.asset.asset_backend.domains.asset.repository;

import com.asset.asset_backend.common.enums.AssetType;
import com.asset.asset_backend.domains.asset.entity.Asset;
import com.asset.asset_backend.domains.asset.entity.QAsset;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.core.types.dsl.BooleanExpression;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RequiredArgsConstructor
public class AssetRepositoryCustomImpl implements AssetRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Asset> searchAssets(String category, String owner, AssetType type, Pageable pageable) {
        QAsset asset = QAsset.asset;

        List<Asset> assets = queryFactory
                .selectFrom(asset)
                .where(
                        containsCategory(category),
                        eqOwner(owner),
                        eqType(type)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(asset.count())
                .from(asset)
                .where(
                        containsCategory(category),
                        eqOwner(owner),
                        eqType(type)
                )
                .fetchOne();

        return new PageImpl<>(assets, pageable, total != null ? total : 0);
    }

    private BooleanExpression containsCategory(String category) {
        return category != null ? QAsset.asset.category.containsIgnoreCase(category) : null;
    }

    private BooleanExpression eqOwner(String owner) {
        return owner != null ? QAsset.asset.owner.eq(owner) : null;
    }

    private BooleanExpression eqType(AssetType type) {
        return type != null ? QAsset.asset.type.eq(type) : null;
    }
}