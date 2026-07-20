package com.sentinela.alpr.cameras;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import com.sentinela.alpr.cameras.api.IssuedApiKeyResponse;
import com.sentinela.alpr.support.AbstractIntegrationTest;

class CameraApiKeyIntegrationTest extends AbstractIntegrationTest {

	@Test
	void adminCanMintKeyReturningPlaintextOnce() {
		Long cameraId = createCamera("Cam");

		ResponseEntity<IssuedApiKeyResponse> response = client.post()
				.uri("/api/v1/cameras/" + cameraId + "/api-keys")
				.retrieve().toEntity(IssuedApiKeyResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().apiKey()).startsWith("alpr_");
		assertThat(response.getBody().keyPrefix()).startsWith("alpr_");
		assertThat(response.getBody().cameraId()).isEqualTo(cameraId);
	}

	@Test
	void operatorCannotMintKey() {
		Long cameraId = createCamera("Cam");
		seedUser("op", "op123456", "OPERATOR");
		RestClient operator = clientFor("op", "op123456");

		ResponseEntity<String> response = operator.post()
				.uri("/api/v1/cameras/" + cameraId + "/api-keys")
				.retrieve().onStatus(s -> s.isError(), (req, res) -> {
				}).toEntity(String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
	}

	@Test
	void listNeverExposesTheSecret() {
		Long cameraId = createCamera("Cam");
		client.post().uri("/api/v1/cameras/" + cameraId + "/api-keys").retrieve().toBodilessEntity();

		ResponseEntity<String> response = client.get()
				.uri("/api/v1/cameras/" + cameraId + "/api-keys")
				.retrieve().toEntity(String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).contains("keyPrefix");
		assertThat(response.getBody()).doesNotContain("apiKey");
	}

	@Test
	void ingestionWorksThenStopsAfterRevoke() {
		Long cameraId = createCamera("Cam");
		IssuedApiKeyResponse issued = client.post().uri("/api/v1/cameras/" + cameraId + "/api-keys")
				.retrieve().body(IssuedApiKeyResponse.class);
		RestClient camera = cameraClient(issued.apiKey());

		assertThat(ingest(camera)).isEqualTo(HttpStatus.CREATED);

		client.delete().uri("/api/v1/cameras/" + cameraId + "/api-keys/" + issued.id())
				.retrieve().toBodilessEntity();

		assertThat(ingest(camera)).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	private HttpStatus ingest(RestClient camera) {
		ResponseEntity<String> response = camera.post().uri("/api/v1/detections")
				.header("Content-Type", "application/json")
				.body("{\"plate\":\"ABC1234\",\"detectedAt\":\"2026-07-18T10:00:00Z\"}")
				.retrieve().onStatus(s -> s.isError(), (req, res) -> {
				}).toEntity(String.class);
		return HttpStatus.valueOf(response.getStatusCode().value());
	}
}
