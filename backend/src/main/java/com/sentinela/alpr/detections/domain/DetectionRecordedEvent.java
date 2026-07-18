package com.sentinela.alpr.detections.domain;

import java.time.Instant;

public record DetectionRecordedEvent(
		Long detectionId,
		String plate,
		Long cameraId,
		Instant detectedAt) {
}
