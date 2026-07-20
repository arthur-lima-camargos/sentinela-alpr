package com.sentinela.alpr.cameras.api;

import java.time.Instant;

public record IssuedApiKeyResponse(
		Long id,
		Long cameraId,
		String apiKey,
		String keyPrefix,
		Instant createdAt) {
}
