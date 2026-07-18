package com.sentinela.alpr.cameras.api;

import java.math.BigDecimal;
import java.time.Instant;

public record CameraResponse(
		Long id,
		String name,
		BigDecimal latitude,
		BigDecimal longitude,
		String road,
		boolean active,
		Instant createdAt) {
}
