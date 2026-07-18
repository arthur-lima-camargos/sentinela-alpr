package com.sentinela.alpr.cameras;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.sentinela.alpr.cameras.api.CameraResponse;
import com.sentinela.alpr.support.AbstractIntegrationTest;

class CameraIntegrationTest extends AbstractIntegrationTest {

	@Test
	void createReturns201AndPersists() {
		ResponseEntity<CameraResponse> response = client.post().uri("/api/v1/cameras")
				.contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("name", "Portal Norte", "road", "BR-101"))
				.retrieve().toEntity(CameraResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().id()).isNotNull();
		assertThat(response.getBody().active()).isTrue();
		assertThat(response.getBody().createdAt()).isNotNull();
	}

	@Test
	void blankNameReturns400() {
		ResponseEntity<String> response = client.post().uri("/api/v1/cameras")
				.contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("name", ""))
				.retrieve().onStatus(status -> status.isError(), (req, res) -> {
				}).toEntity(String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).contains("name");
	}

	@Test
	void getUnknownReturns404() {
		ResponseEntity<String> response = client.get().uri("/api/v1/cameras/9999")
				.retrieve().onStatus(status -> status.isError(), (req, res) -> {
				}).toEntity(String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void deactivateMakesCameraInactive() {
		Long id = client.post().uri("/api/v1/cameras")
				.contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("name", "Cam A"))
				.retrieve().body(CameraResponse.class).id();

		client.delete().uri("/api/v1/cameras/" + id).retrieve().toBodilessEntity();

		CameraResponse after = client.get().uri("/api/v1/cameras/" + id)
				.retrieve().body(CameraResponse.class);
		assertThat(after).isNotNull();
		assertThat(after.active()).isFalse();
	}
}
