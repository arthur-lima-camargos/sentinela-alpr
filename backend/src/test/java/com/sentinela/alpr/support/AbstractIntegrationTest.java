package com.sentinela.alpr.support;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestClient;

import com.sentinela.alpr.TestcontainersConfiguration;
import com.sentinela.alpr.auth.api.TokenResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
public abstract class AbstractIntegrationTest {

	@LocalServerPort
	private int port;

	@Autowired
	private JdbcTemplate jdbc;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private String baseUrl;

	protected RestClient anonymousClient;

	protected RestClient client;

	@BeforeEach
	void setUp() {
		jdbc.execute("truncate table alert, detection, watched_vehicle, camera, app_user restart identity cascade");
		baseUrl = "http://localhost:" + port;
		anonymousClient = RestClient.builder().baseUrl(baseUrl).build();
		seedUser("admin", "admin123", "ADMIN");
		client = clientFor("admin", "admin123");
	}

	protected void seedUser(String login, String rawPassword, String role) {
		seedUser(login, rawPassword, role, true);
	}

	protected void seedUser(String login, String rawPassword, String role, boolean active) {
		jdbc.update("insert into app_user (login, password_hash, role, active) values (?, ?, ?, ?)",
				login, passwordEncoder.encode(rawPassword), role, active);
	}

	protected String accessToken(String login, String rawPassword) {
		TokenResponse tokens = anonymousClient.post().uri("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("login", login, "password", rawPassword))
				.retrieve().body(TokenResponse.class);
		return tokens.accessToken();
	}

	protected RestClient clientWithToken(String token) {
		return RestClient.builder().baseUrl(baseUrl)
				.defaultHeaders(h -> h.setBearerAuth(token)).build();
	}

	protected RestClient clientFor(String login, String rawPassword) {
		return clientWithToken(accessToken(login, rawPassword));
	}
}
