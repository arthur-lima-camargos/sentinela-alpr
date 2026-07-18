package com.sentinela.alpr.detections.api;

import java.time.Instant;

public record DetectionResponse(
		Long id,
		String plate,
		Long cameraId,
		Instant detectedAt) {
}
