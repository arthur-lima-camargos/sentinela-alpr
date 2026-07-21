package com.sentinela.alpr.watchlist;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.sentinela.alpr.support.AbstractIntegrationTest;
import com.sentinela.alpr.watchlist.api.WatchlistResponse;
import com.sentinela.alpr.watchlist.api.WatchlistSummaryResponse;

class WatchlistSummaryIntegrationTest extends AbstractIntegrationTest {

	private Long watch(String plate) {
		return client.post().uri("/api/v1/watchlist")
				.contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("plate", plate, "reason", "ROBBERY"))
				.retrieve().body(WatchlistResponse.class).id();
	}

	@Test
	void summaryCountsOnlyActive() {
		watch("ABC1234");
		watch("DEF5678");
		Long inactive = watch("GHI9012");
		client.delete().uri("/api/v1/watchlist/" + inactive).retrieve().toBodilessEntity();

		WatchlistSummaryResponse summary = client.get().uri("/api/v1/watchlist/summary")
				.retrieve().body(WatchlistSummaryResponse.class);

		assertThat(summary).isNotNull();
		assertThat(summary.active()).isEqualTo(2);
	}

	@Test
	void summaryRequiresAuthentication() {
		ResponseEntity<String> response = anonymousClient.get().uri("/api/v1/watchlist/summary")
				.retrieve().onStatus(status -> status.isError(), (req, res) -> {
				}).toEntity(String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}
}
