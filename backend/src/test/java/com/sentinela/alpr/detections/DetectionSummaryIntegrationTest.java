package com.sentinela.alpr.detections;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import com.sentinela.alpr.detections.api.DetectionSummaryResponse;
import com.sentinela.alpr.support.AbstractIntegrationTest;

class DetectionSummaryIntegrationTest extends AbstractIntegrationTest {

	private void detect(RestClient camera, String plate, Instant detectedAt) {
		Map<String, Object> body = new HashMap<>();
		body.put("plate", plate);
		body.put("detectedAt", detectedAt.toString());
		camera.post().uri("/api/v1/detections")
				.contentType(MediaType.APPLICATION_JSON)
				.body(body)
				.retrieve().toBodilessEntity();
	}

	@Test
	void summaryCountsRollingWindows() {
		Long cameraId = createCamera("Cam");
		RestClient camera = cameraClient(mintApiKey(cameraId));
		Instant now = Instant.now();

		detect(camera, "ABC1234", now.minus(Duration.ofSeconds(30)));
		detect(camera, "DEF5678", now.minus(Duration.ofHours(2)));
		detect(camera, "GHI9012", now.minus(Duration.ofHours(48)));

		DetectionSummaryResponse summary = client.get().uri("/api/v1/detections/summary")
				.retrieve().body(DetectionSummaryResponse.class);

		assertThat(summary).isNotNull();
		assertThat(summary.lastHour()).isEqualTo(1);
		assertThat(summary.last24h()).isEqualTo(2);
	}

	@Test
	void summaryRequiresAuthentication() {
		ResponseEntity<String> response = anonymousClient.get().uri("/api/v1/detections/summary")
				.retrieve().onStatus(status -> status.isError(), (req, res) -> {
				}).toEntity(String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}
}
