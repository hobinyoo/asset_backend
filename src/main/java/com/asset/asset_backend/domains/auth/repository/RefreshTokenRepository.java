package com.asset.asset_backend.domains.auth.repository;

import com.asset.asset_backend.domains.auth.entity.RefreshToken;
import com.asset.asset_backend.domains.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user);
}
