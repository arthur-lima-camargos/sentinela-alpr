package com.sentinela.alpr.alerts;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sentinela.alpr.alerts.api.AlertResponse;
import com.sentinela.alpr.alerts.api.AlertSummaryResponse;
import com.sentinela.alpr.support.AbstractIntegrationTest;

class AlertSummaryIntegrationTest extends AbstractIntegrationTest {

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record AlertPage(List<AlertResponse> content) {
	}

	private void watch(String plate) {
		client.post().uri("/api/v1/watchlist")
				.contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("plate", plate, "reason", "ROBBERY"))
				.retrieve().toBodilessEntity();
	}

	private void detect(RestClient camera, String plate, String detectedAt) {
		Map<String, Object> body = new HashMap<>();
		body.put("plate", plate);
		body.put("detectedAt", detectedAt);
		camera.post().uri("/api/v1/detections")
				.contentType(MediaType.APPLICATION_JSON)
				.body(body)
				.retrieve().toBodilessEntity();
	}

	@Test
	void summaryCountsNewAndSeen() {
		watch("ABC1234");
		RestClient camera = cameraClient(mintApiKey(createCamera("Cam")));
		detect(camera, "ABC1234", "2026-07-18T10:00:00Z");
		detect(camera, "ABC1234", "2026-07-18T11:00:00Z");

		AlertPage page = client.get().uri("/api/v1/alerts").retrieve().body(AlertPage.class);
		assertThat(page.content()).hasSize(2);
		Long first = page.content().get(0).id();
		client.patch().uri("/api/v1/alerts/" + first)
				.contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("status", "SEEN"))
				.retrieve().toBodilessEntity();

		AlertSummaryResponse summary = client.get().uri("/api/v1/alerts/summary")
				.retrieve().body(AlertSummaryResponse.class);

		assertThat(summary).isNotNull();
		assertThat(summary.newCount()).isEqualTo(1);
		assertThat(summary.seenCount()).isEqualTo(1);
	}

	@Test
	void summaryRequiresAuthentication() {
		ResponseEntity<String> response = anonymousClient.get().uri("/api/v1/alerts/summary")
				.retrieve().onStatus(status -> status.isError(), (req, res) -> {
				}).toEntity(String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}
}
