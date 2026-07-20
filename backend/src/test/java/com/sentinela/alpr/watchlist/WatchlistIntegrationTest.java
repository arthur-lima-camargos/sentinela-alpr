package com.sentinela.alpr.watchlist;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import com.sentinela.alpr.support.AbstractIntegrationTest;
import com.sentinela.alpr.watchlist.api.WatchlistResponse;

class WatchlistIntegrationTest extends AbstractIntegrationTest {

	private ResponseEntity<String> add(Object body) {
		return client.post().uri("/api/v1/watchlist")
				.contentType(MediaType.APPLICATION_JSON)
				.body(body)
				.retrieve().onStatus(status -> status.isError(), (req, res) -> {
				}).toEntity(String.class);
	}

	private Long addPlate(String plate, String reason) {
		return client.post().uri("/api/v1/watchlist")
				.contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("plate", plate, "reason", reason))
				.retrieve().body(WatchlistResponse.class).id();
	}

	@Test
	void addReturns201AndNormalizesPlate() {
		WatchlistResponse body = client.post().uri("/api/v1/watchlist")
				.contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("plate", "abc-1d34", "reason", "ROBBERY"))
				.retrieve().body(WatchlistResponse.class);

		assertThat(body).isNotNull();
		assertThat(body.plate()).isEqualTo("ABC1D34");
		assertThat(body.active()).isTrue();
	}

	@Test
	void duplicatePlateReturns409() {
		add(Map.of("plate", "ABC1234", "reason", "THEFT"));

		ResponseEntity<String> second = add(Map.of("plate", "ABC1234", "reason", "WANTED"));

		assertThat(second.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
	}

	@Test
	void invalidPlateReturns422() {
		ResponseEntity<String> response = add(Map.of("plate", "XX", "reason", "SUSPECT"));
		assertThat(response.getStatusCode().value()).isEqualTo(422);
	}

	@Test
	void missingReasonReturns400() {
		ResponseEntity<String> response = add(Map.of("plate", "ABC1234"));
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void deactivateMakesItemInactive() {
		Long id = addPlate("ABC1234", "THEFT");

		client.delete().uri("/api/v1/watchlist/" + id).retrieve().toBodilessEntity();

		WatchlistResponse after = client.get().uri("/api/v1/watchlist/" + id)
				.retrieve().body(WatchlistResponse.class);
		assertThat(after).isNotNull();
		assertThat(after.active()).isFalse();
	}

	@Test
	void activateReactivatesDeactivatedItem() {
		Long id = addPlate("ABC1234", "THEFT");

		client.delete().uri("/api/v1/watchlist/" + id).retrieve().toBodilessEntity();
		client.post().uri("/api/v1/watchlist/" + id + "/activate").retrieve().toBodilessEntity();

		WatchlistResponse after = client.get().uri("/api/v1/watchlist/" + id)
				.retrieve().body(WatchlistResponse.class);
		assertThat(after).isNotNull();
		assertThat(after.active()).isTrue();
	}

	@Test
	void updateReclassifiesReason() {
		Long id = addPlate("ABC1234", "THEFT");

		WatchlistResponse updated = client.put().uri("/api/v1/watchlist/" + id)
				.contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("reason", "WANTED"))
				.retrieve().body(WatchlistResponse.class);

		assertThat(updated).isNotNull();
		assertThat(updated.reason().name()).isEqualTo("WANTED");
		assertThat(updated.plate()).isEqualTo("ABC1234");
	}

	@Test
	void updateWithInvalidReasonReturns400() {
		Long id = addPlate("ABC1234", "THEFT");

		ResponseEntity<String> response = client.put().uri("/api/v1/watchlist/" + id)
				.contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("reason", ""))
				.retrieve().onStatus(status -> status.isError(), (req, res) -> {
				}).toEntity(String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void getUnknownReturns404() {
		ResponseEntity<String> response = client.get().uri("/api/v1/watchlist/9999")
				.retrieve().onStatus(status -> status.isError(), (req, res) -> {
				}).toEntity(String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void activateUnknownReturns404() {
		ResponseEntity<String> response = client.post().uri("/api/v1/watchlist/9999/activate")
				.retrieve().onStatus(status -> status.isError(), (req, res) -> {
				}).toEntity(String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void operatorCannotAddReturns403() {
		seedUser("operador", "op123", "OPERATOR");
		RestClient operator = clientFor("operador", "op123");

		ResponseEntity<String> response = operator.post().uri("/api/v1/watchlist")
				.contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("plate", "ABC1234", "reason", "THEFT"))
				.retrieve().onStatus(status -> status.isError(), (req, res) -> {
				}).toEntity(String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
	}

	@Test
	void operatorCannotDeactivateReturns403() {
		Long id = addPlate("ABC1234", "THEFT");
		seedUser("operador", "op123", "OPERATOR");
		RestClient operator = clientFor("operador", "op123");

		ResponseEntity<String> response = operator.delete().uri("/api/v1/watchlist/" + id)
				.retrieve().onStatus(status -> status.isError(), (req, res) -> {
				}).toEntity(String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
	}

	@Test
	void operatorCanListReturns200() {
		seedUser("operador", "op123", "OPERATOR");
		RestClient operator = clientFor("operador", "op123");

		ResponseEntity<String> response = operator.get().uri("/api/v1/watchlist")
				.retrieve().toEntity(String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	void anonymousCannotAccessReturns401() {
		ResponseEntity<String> response = anonymousClient.get().uri("/api/v1/watchlist")
				.retrieve().onStatus(status -> status.isError(), (req, res) -> {
				}).toEntity(String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}
}
