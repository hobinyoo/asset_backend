package com.asset.asset_backend.domains.config.service;

import com.asset.asset_backend.common.exception.BaseException;
import com.asset.asset_backend.common.exception.ErrorCode;
import com.asset.asset_backend.domains.auth.entity.User;
import com.asset.asset_backend.domains.auth.repository.UserRepository;
import com.asset.asset_backend.domains.config.entity.ConfigType;
import com.asset.asset_backend.domains.config.entity.UserConfig;
import com.asset.asset_backend.domains.config.repository.UserConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserConfigService {

    private final UserConfigRepository userConfigRepository;
    private final UserRepository userRepository;

    public List<UserConfig> getConfigs(Long userId, ConfigType configType) {
        return userConfigRepository.findByUser_IdAndConfigTypeOrderByCreatedAtAsc(userId, configType);
    }

    @Transactional
    public UserConfig addConfig(Long userId, ConfigType configType, String value) {
        if (userConfigRepository.existsByUser_IdAndConfigTypeAndValue(userId, configType, value)) {
            throw new BaseException(ErrorCode.ALREADY_EXIST, "이미 존재하는 값입니다: " + value);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND, "User ID: " + userId));
        return userConfigRepository.save(UserConfig.create(user, configType, value));
    }

    @Transactional
    public void createDefaultConfigs(User user) {
        List<String> assetCategories = List.of("전세보증금", "청약저축", "청년도약적금", "IRP", "DC", "연금저축", "기타");
        List<String> investmentCategories = List.of("ETF", "주식", "금", "현금", "기타");

        assetCategories.forEach(v -> userConfigRepository.save(UserConfig.create(user, ConfigType.ASSET_CATEGORY, v)));
        investmentCategories.forEach(v -> userConfigRepository.save(UserConfig.create(user, ConfigType.INVESTMENT_CATEGORY, v)));
    }

    @Transactional
    public void deleteConfig(Long configId, Long userId) {
        UserConfig config = userConfigRepository.findById(configId)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Config ID: " + configId));
        if (!config.getUser().getId().equals(userId)) {
            throw new BaseException(ErrorCode.FORBIDDEN, "해당 설정에 접근 권한이 없습니다");
        }
        userConfigRepository.delete(config);
    }
}