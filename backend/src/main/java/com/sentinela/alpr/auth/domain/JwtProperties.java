package com.sentinela.alpr.auth.domain;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtProperties(
		String secret,
		Duration accessTtl,
		Duration refreshTtl) {
}
