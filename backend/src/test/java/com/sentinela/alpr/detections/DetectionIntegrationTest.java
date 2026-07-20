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
import org.springframework.web.client.RestClient;

import com.sentinela.alpr.cameras.api.IssuedApiKeyResponse;
import com.sentinela.alpr.detections.api.DetectionPage;
import com.sentinela.alpr.detections.api.DetectionResponse;
import com.sentinela.alpr.support.AbstractIntegrationTest;

class DetectionIntegrationTest extends AbstractIntegrationTest {

	private Map<String, Object> body(String plate, String detectedAt) {
		Map<String, Object> body = new HashMap<>();
		body.put("plate", plate);
		body.put("detectedAt", detectedAt);
		return body;
	}

	private ResponseEntity<String> postWith(RestClient camera, Map<String, Object> body) {
		return camera.post().uri("/api/v1/detections")
				.contentType(MediaType.APPLICATION_JSON)
				.body(body)
				.retrieve().onStatus(status -> status.isError(), (req, res) -> {
				}).toEntity(String.class);
	}

	@Test
	void recordWithValidApiKeyReturns201AndDerivesCamera() {
		Long cameraId = createCamera("Cam");
		RestClient camera = cameraClient(mintApiKey(cameraId));

		DetectionResponse created = camera.post().uri("/api/v1/detections")
				.contentType(MediaType.APPLICATION_JSON)
				.body(body("ABC1234", "2026-07-18T10:00:00Z"))
				.retrieve().toEntity(DetectionResponse.class).getBody();

		assertThat(created).isNotNull();
		assertThat(created.id()).isNotNull();
		assertThat(created.plate()).isEqualTo("ABC1234");
		assertThat(created.cameraId()).isEqualTo(cameraId);
	}

	@Test
	void recordWithoutApiKeyReturns401() {
		ResponseEntity<String> response = postWith(anonymousClient, body("ABC1234", "2026-07-18T10:00:00Z"));
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void recordWithInvalidApiKeyReturns401() {
		ResponseEntity<String> response = postWith(cameraClient("alpr_invalida"), body("ABC1234", "2026-07-18T10:00:00Z"));
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void recordWithRevokedApiKeyReturns401() {
		Long cameraId = createCamera("Cam");
		IssuedApiKeyResponse issued = client.post().uri("/api/v1/cameras/" + cameraId + "/api-keys")
				.retrieve().body(IssuedApiKeyResponse.class);
		client.delete().uri("/api/v1/cameras/" + cameraId + "/api-keys/" + issued.id())
				.retrieve().toBodilessEntity();

		ResponseEntity<String> response = postWith(cameraClient(issued.apiKey()),
				body("ABC1234", "2026-07-18T10:00:00Z"));
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void recordWithInactiveCameraReturns401() {
		Long cameraId = createCamera("Cam");
		RestClient camera = cameraClient(mintApiKey(cameraId));
		client.delete().uri("/api/v1/cameras/" + cameraId).retrieve().toBodilessEntity();

		ResponseEntity<String> response = postWith(camera, body("ABC1234", "2026-07-18T10:00:00Z"));
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void keysetPaginationWalksAllRowsInOrderWithoutDuplicates() {
		Long cameraId = createCamera("Cam");
		RestClient camera = cameraClient(mintApiKey(cameraId));
		for (int i = 0; i < 5; i++) {
			postWith(camera, body("ABC1234", "2026-07-18T10:0" + i + ":00Z"));
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
