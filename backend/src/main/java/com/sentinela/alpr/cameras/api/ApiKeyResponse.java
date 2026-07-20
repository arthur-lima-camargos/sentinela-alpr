package com.sentinela.alpr.cameras.api;

import java.time.Instant;

public record ApiKeyResponse(
		Long id,
		Long cameraId,
		String keyPrefix,
		boolean active,
		Instant createdAt,
		Instant revokedAt) {
}
