package com.asset.asset_backend.domains.auth.dto.response;
import com.asset.asset_backend.domains.auth.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserResponse {

    private Long id;
    private String loginId;
    private LocalDateTime createdAt;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .loginId(user.getLoginId())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
