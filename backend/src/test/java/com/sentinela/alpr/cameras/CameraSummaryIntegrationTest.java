package com.sentinela.alpr.cameras;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.sentinela.alpr.cameras.api.CameraSummaryResponse;
import com.sentinela.alpr.support.AbstractIntegrationTest;

class CameraSummaryIntegrationTest extends AbstractIntegrationTest {

	@Test
	void summaryCountsActiveAndInactive() {
		createCamera("Cam A");
		createCamera("Cam B");
		Long third = createCamera("Cam C");
		client.delete().uri("/api/v1/cameras/" + third).retrieve().toBodilessEntity();

		CameraSummaryResponse summary = client.get().uri("/api/v1/cameras/summary")
				.retrieve().body(CameraSummaryResponse.class);

		assertThat(summary).isNotNull();
		assertThat(summary.active()).isEqualTo(2);
		assertThat(summary.inactive()).isEqualTo(1);
	}

	@Test
	void summaryRequiresAuthentication() {
		ResponseEntity<String> response = anonymousClient.get().uri("/api/v1/cameras/summary")
				.retrieve().onStatus(status -> status.isError(), (req, res) -> {
				}).toEntity(String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}
}
