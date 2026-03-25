package com.asset.asset_backend.domains.config.repository;

import com.asset.asset_backend.domains.config.entity.ConfigType;
import com.asset.asset_backend.domains.config.entity.UserConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserConfigRepository extends JpaRepository<UserConfig, Long> {
    List<UserConfig> findByUser_IdAndConfigTypeOrderByCreatedAtAsc(Long userId, ConfigType configType);
    boolean existsByUser_IdAndConfigTypeAndValue(Long userId, ConfigType configType, String value);
}