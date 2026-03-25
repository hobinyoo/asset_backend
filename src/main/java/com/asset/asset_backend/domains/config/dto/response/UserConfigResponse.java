package com.asset.asset_backend.domains.config.dto.response;

import com.asset.asset_backend.domains.config.entity.UserConfig;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserConfigResponse {
    private Long id;        // 기본값이면 null (삭제 불가)
    private String value;

    public static UserConfigResponse from(UserConfig config) {
        return UserConfigResponse.builder()
                .id(config.getId())
                .value(config.getValue())
                .build();
    }

    public static UserConfigResponse ofDefault(String value) {
        return UserConfigResponse.builder()
                .id(null)
                .value(value)
                .build();
    }
}