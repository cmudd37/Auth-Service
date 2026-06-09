package com.example.authservice.config;

import com.example.authservice.token.RefreshTokenProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RefreshTokenProperties.class)
public class RefreshTokenConfig {
}
