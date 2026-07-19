package com.sentinela.alpr.auth.api;

public record TokenResponse(
		String accessToken,
		String refreshToken,
		long expiresIn,
		String tokenType) {
}
