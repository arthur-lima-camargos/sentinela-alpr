package com.sentinela.alpr.support;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestClient;

import com.sentinela.alpr.TestcontainersConfiguration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
public abstract class AbstractIntegrationTest {

	@LocalServerPort
	private int port;

	@Autowired
	private JdbcTemplate jdbc;

	protected RestClient client;

	@BeforeEach
	void setUp() {
		jdbc.execute("truncate table alert, detection, watched_vehicle, camera, app_user restart identity cascade");
		client = RestClient.builder().baseUrl("http://localhost:" + port).build();
	}
}
