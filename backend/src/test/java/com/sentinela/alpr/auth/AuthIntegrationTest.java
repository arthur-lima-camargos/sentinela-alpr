package com.sentinela.alpr.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import com.sentinela.alpr.auth.api.TokenResponse;
import com.sentinela.alpr.support.AbstractIntegrationTest;

class AuthIntegrationTest extends AbstractIntegrationTest {

	@Test
	void loginWithValidCredentialsReturnsTokens() {
		ResponseEntity<TokenResponse> response = anonymousClient.post().uri("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("login", "admin", "password", "admin123"))
				.retrieve().toEntity(TokenResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().accessToken()).isNotBlank();
		assertThat(response.getBody().refreshToken()).isNotBlank();
		assertThat(response.getBody().tokenType()).isEqualTo("Bearer");
		assertThat(response.getBody().expiresIn()).isPositive();
	}

	@Test
	void loginWithWrongPasswordReturns401() {
		assertThat(loginStatus("admin", "wrong")).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void loginWithUnknownUserReturns401() {
		assertThat(loginStatus("ghost", "whatever")).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void loginWithInactiveUserReturns401() {
		seedUser("disabled", "secret123", "OPERATOR", false);
		assertThat(loginStatus("disabled", "secret123")).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void blankLoginReturns400() {
		ResponseEntity<String> response = anonymousClient.post().uri("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("login", "", "password", "x"))
				.retrieve().onStatus(s -> s.isError(), (req, res) -> {
				}).toEntity(String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void protectedRouteWithoutTokenReturns401() {
		ResponseEntity<String> response = anonymousClient.get().uri("/api/v1/cameras")
				.retrieve().onStatus(s -> s.isError(), (req, res) -> {
				}).toEntity(String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void operatorCannotCreateCamera() {
		seedUser("op", "op123456", "OPERATOR");
		RestClient operator = clientFor("op", "op123456");

		ResponseEntity<String> response = operator.post().uri("/api/v1/cameras")
				.contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("name", "Cam X"))
				.retrieve().onStatus(s -> s.isError(), (req, res) -> {
				}).toEntity(String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
	}

	@Test
	void operatorCanReadCameras() {
		seedUser("op", "op123456", "OPERATOR");
		RestClient operator = clientFor("op", "op123456");

		ResponseEntity<String> response = operator.get().uri("/api/v1/cameras")
				.retrieve().toEntity(String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	void refreshWithValidRefreshTokenReturnsNewTokens() {
		TokenResponse initial = anonymousClient.post().uri("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("login", "admin", "password", "admin123"))
				.retrieve().body(TokenResponse.class);

		ResponseEntity<TokenResponse> response = anonymousClient.post().uri("/api/v1/auth/refresh")
				.contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("refreshToken", initial.refreshToken()))
				.retrieve().toEntity(TokenResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().accessToken()).isNotBlank();
		assertThat(response.getBody().refreshToken()).isNotBlank();
	}

	@Test
	void accessTokenCannotBeUsedToRefresh() {
		TokenResponse initial = anonymousClient.post().uri("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("login", "admin", "password", "admin123"))
				.retrieve().body(TokenResponse.class);

		ResponseEntity<String> response = anonymousClient.post().uri("/api/v1/auth/refresh")
				.contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("refreshToken", initial.accessToken()))
				.retrieve().onStatus(s -> s.isError(), (req, res) -> {
				}).toEntity(String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void garbageRefreshTokenReturns401() {
		ResponseEntity<String> response = anonymousClient.post().uri("/api/v1/auth/refresh")
				.contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("refreshToken", "not-a-jwt"))
				.retrieve().onStatus(s -> s.isError(), (req, res) -> {
				}).toEntity(String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	private HttpStatus loginStatus(String login, String password) {
		ResponseEntity<String> response = anonymousClient.post().uri("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("login", login, "password", password))
				.retrieve().onStatus(s -> s.isError(), (req, res) -> {
				}).toEntity(String.class);
		return HttpStatus.valueOf(response.getStatusCode().value());
	}
}
