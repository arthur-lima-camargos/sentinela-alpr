package com.sentinela.alpr.detections.api;

import java.time.Instant;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

public record DetectionRequest(

		@NotBlank String plate,

		@NotNull Instant detectedAt) {
}
