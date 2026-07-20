package com.sentinela.alpr.alerts;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sentinela.alpr.alerts.api.AlertResponse;
import com.sentinela.alpr.alerts.domain.AlertStatus;
import com.sentinela.alpr.support.AbstractIntegrationTest;

class AlertFlowIntegrationTest extends AbstractIntegrationTest {

	private static final String WATCHED_PLATE = "ABC1234";

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record AlertPage(List<AlertResponse> content) {
	}

	private Long camera() {
		return createCamera("Cam");
	}

	private void watch(String plate) {
		client.post().uri("/api/v1/watchlist")
				.contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("plate", plate, "reason", "ROBBERY"))
				.retrieve().toBodilessEntity();
	}

	private void detect(Long cameraId, String plate) {
		Map<String, Object> body = new HashMap<>();
		body.put("plate", plate);
		body.put("detectedAt", "2026-07-18T10:00:00Z");
		cameraClient(mintApiKey(cameraId)).post().uri("/api/v1/detections")
				.contentType(MediaType.APPLICATION_JSON)
				.body(body)
				.retrieve().toBodilessEntity();
	}

	private AlertResponse firstAlert() {
		AlertPage page = client.get().uri("/api/v1/alerts").retrieve().body(AlertPage.class);
		return page.content().isEmpty() ? null : page.content().get(0);
	}

	@Test
	void detectionOnWatchedPlateGeneratesAlert() {
		watch(WATCHED_PLATE);
		detect(camera(), WATCHED_PLATE);

		AlertResponse alert = firstAlert();
		assertThat(alert).isNotNull();
		assertThat(alert.plate()).isEqualTo(WATCHED_PLATE);
		assertThat(alert.status()).isEqualTo(AlertStatus.NEW);
	}

	@Test
	void detectionOnUnwatchedPlateGeneratesNoAlert() {
		watch(WATCHED_PLATE);
		detect(camera(), "XYZ9876");

		assertThat(firstAlert()).isNull();
	}

	@Test
	void alertCanBeMarkedSeen() {
		watch(WATCHED_PLATE);
		detect(camera(), WATCHED_PLATE);
		Long id = firstAlert().id();

		ResponseEntity<AlertResponse> patched = client.patch().uri("/api/v1/alerts/" + id)
				.contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("status", "SEEN"))
				.retrieve().toEntity(AlertResponse.class);

		assertThat(patched.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(patched.getBody().status()).isEqualTo(AlertStatus.SEEN);
	}
}
