package com.sentinela.alpr.detections.api;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DetectionRequest(

		@NotBlank String plate,

		@NotNull Long cameraId,

		@NotNull Instant detectedAt) {
}
