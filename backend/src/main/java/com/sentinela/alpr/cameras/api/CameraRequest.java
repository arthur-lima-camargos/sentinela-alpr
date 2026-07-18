package com.sentinela.alpr.cameras.api;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CameraRequest(

		@NotBlank @Size(max = 120) String name,

		@DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal latitude,

		@DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal longitude,

		@Size(max = 120) String road) {
}
