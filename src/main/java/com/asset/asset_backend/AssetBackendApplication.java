package com.asset.asset_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AssetBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(AssetBackendApplication.class, args);
    }

}
