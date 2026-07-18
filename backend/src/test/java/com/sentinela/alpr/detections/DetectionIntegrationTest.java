package com.sentinela.alpr.detections;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.sentinela.alpr.cameras.api.CameraResponse;
import com.sentinela.alpr.detections.api.DetectionPage;
import com.sentinela.alpr.detections.api.DetectionResponse;
import com.sentinela.alpr.support.AbstractIntegrationTest;

class DetectionIntegrationTest extends AbstractIntegrationTest {

	private Long activeCamera() {
		return client.post().uri("/api/v1/cameras")
				.contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("name", "Cam"))
				.retrieve().body(CameraResponse.class).id();
	}

	private Map<String, Object> body(Object cameraId, String plate, String detectedAt) {
		Map<String, Object> body = new HashMap<>();
		body.put("plate", plate);
		body.put("cameraId", cameraId);
		body.put("detectedAt", detectedAt);
		return body;
	}

	private ResponseEntity<String> post(Map<String, Object> body) {
		return client.post().uri("/api/v1/detections")
				.contentType(MediaType.APPLICATION_JSON)
				.body(body)
				.retrieve().onStatus(status -> status.isError(), (req, res) -> {
				}).toEntity(String.class);
	}

	@Test
	void recordWithActiveCameraReturns201() {
		DetectionResponse created = client.post().uri("/api/v1/detections")
				.contentType(MediaType.APPLICATION_JSON)
				.body(body(activeCamera(), "ABC1234", "2026-07-18T10:00:00Z"))
				.retrieve().toEntity(DetectionResponse.class).getBody();

		assertThat(created).isNotNull();
		assertThat(created.id()).isNotNull();
		assertThat(created.plate()).isEqualTo("ABC1234");
	}

	@Test
	void recordWithUnknownCameraReturns404() {
		ResponseEntity<String> response = post(body(9999, "ABC1234", "2026-07-18T10:00:00Z"));
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void recordWithInactiveCameraReturns422() {
		Long cameraId = activeCamera();
		client.delete().uri("/api/v1/cameras/" + cameraId).retrieve().toBodilessEntity();

		ResponseEntity<String> response = post(body(cameraId, "ABC1234", "2026-07-18T10:00:00Z"));
		assertThat(response.getStatusCode().value()).isEqualTo(422);
	}

	@Test
	void keysetPaginationWalksAllRowsInOrderWithoutDuplicates() {
		Long cameraId = activeCamera();
		for (int i = 0; i < 5; i++) {
			post(body(cameraId, "ABC1234", "2026-07-18T10:0" + i + ":00Z"));
		}

		Set<Long> seen = new LinkedHashSet<>();
		String cursor = null;
		int pages = 0;
		do {
			String url = "/api/v1/detections?size=2" + (cursor == null ? "" : "&cursor=" + cursor);
			DetectionPage page = client.get().uri(url).retrieve().body(DetectionPage.class);
			assertThat(page).isNotNull();
			page.content().forEach(d -> seen.add(d.id()));
			cursor = page.nextCursor();
			pages++;
		}
		while (cursor != null && pages < 10);

		assertThat(seen).hasSize(5);
		assertThat(pages).isEqualTo(3);
	}
}
