package com.sentinela.alpr.cameras;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

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

	@Test
	void activateReactivatesDeactivatedCamera() {
		Long id = client.post().uri("/api/v1/cameras")
				.contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("name", "Cam B"))
				.retrieve().body(CameraResponse.class).id();

		client.delete().uri("/api/v1/cameras/" + id).retrieve().toBodilessEntity();
		client.post().uri("/api/v1/cameras/" + id + "/activate").retrieve().toBodilessEntity();

		CameraResponse after = client.get().uri("/api/v1/cameras/" + id)
				.retrieve().body(CameraResponse.class);
		assertThat(after).isNotNull();
		assertThat(after.active()).isTrue();
	}

	@Test
	void updateChangesFields() {
		Long id = createCamera("Antiga");

		CameraResponse updated = client.put().uri("/api/v1/cameras/" + id)
				.contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("name", "Nova", "road", "BR-116"))
				.retrieve().body(CameraResponse.class);

		assertThat(updated.name()).isEqualTo("Nova");
		assertThat(updated.road()).isEqualTo("BR-116");
	}

	@Test
	void invalidLatitudeReturns400() {
		ResponseEntity<String> response = client.post().uri("/api/v1/cameras")
				.contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("name", "Cam", "latitude", 200))
				.retrieve().onStatus(status -> status.isError(), (req, res) -> {
				}).toEntity(String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void activateUnknownReturns404() {
		ResponseEntity<String> response = client.post().uri("/api/v1/cameras/9999/activate")
				.retrieve().onStatus(status -> status.isError(), (req, res) -> {
				}).toEntity(String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void operatorCannotCreateReturns403() {
		seedUser("operador", "op123", "OPERATOR");
		RestClient operator = clientFor("operador", "op123");

		ResponseEntity<String> response = operator.post().uri("/api/v1/cameras")
				.contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("name", "Cam"))
				.retrieve().onStatus(status -> status.isError(), (req, res) -> {
				}).toEntity(String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
	}

	@Test
	void operatorCanListReturns200() {
		seedUser("operador", "op123", "OPERATOR");
		RestClient operator = clientFor("operador", "op123");

		ResponseEntity<String> response = operator.get().uri("/api/v1/cameras")
				.retrieve().toEntity(String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	void anonymousCannotAccessReturns401() {
		ResponseEntity<String> response = anonymousClient.get().uri("/api/v1/cameras")
				.retrieve().onStatus(status -> status.isError(), (req, res) -> {
				}).toEntity(String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}
}
