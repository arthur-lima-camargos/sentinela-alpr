package com.sentinela.alpr.alerts.api;

import java.time.Instant;

import com.sentinela.alpr.alerts.domain.AlertStatus;

public record AlertResponse(
		Long id,
		String plate,
		Long detectionId,
		Long watchedVehicleId,
		Instant detectedAt,
		AlertStatus status,
		Instant createdAt) {
}
