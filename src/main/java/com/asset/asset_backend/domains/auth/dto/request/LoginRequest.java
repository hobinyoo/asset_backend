package com.asset.asset_backend.domains.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class LoginRequest {

    @NotBlank
    private String loginId;

    @NotBlank
    private String password;
}