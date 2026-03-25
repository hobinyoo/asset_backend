package com.asset.asset_backend.domains.config.entity;

import com.asset.asset_backend.domains.auth.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_config")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ConfigType configType;

    @Column(nullable = false, length = 100)
    private String value;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public static UserConfig create(User user, ConfigType configType, String value) {
        UserConfig config = new UserConfig();
        config.user = user;
        config.configType = configType;
        config.value = value;
        return config;
    }
}