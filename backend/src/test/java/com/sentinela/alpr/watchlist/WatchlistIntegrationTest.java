package com.sentinela.alpr.watchlist;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

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
}
