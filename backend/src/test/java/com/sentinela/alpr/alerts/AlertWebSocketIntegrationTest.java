package com.sentinela.alpr.alerts;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import com.sentinela.alpr.support.AbstractIntegrationTest;

class AlertWebSocketIntegrationTest extends AbstractIntegrationTest {

	private static final String WATCHED_PLATE = "ABC1234";

	private WebSocketStompClient stompClient() {
		return new WebSocketStompClient(new StandardWebSocketClient());
	}

	private StompSession connect(String bearerToken) throws Exception {
		StompHeaders connectHeaders = new StompHeaders();
		if (bearerToken != null) {
			connectHeaders.add("Authorization", "Bearer " + bearerToken);
		}
		return stompClient()
				.connectAsync(wsUrl(), new WebSocketHttpHeaders(), connectHeaders,
						new StompSessionHandlerAdapter() {
						})
				.get(5, SECONDS);
	}

	@Test
	void authenticatedClientConnectsAndReceivesAlert() throws Exception {
		StompSession session = connect(accessToken("admin", "admin123"));
		assertThat(session.isConnected()).isTrue();

		BlockingQueue<String> received = new LinkedBlockingQueue<>();
		session.subscribe("/topic/alerts", new StompFrameHandler() {
			@Override
			public Type getPayloadType(StompHeaders headers) {
				return byte[].class;
			}

			@Override
			public void handleFrame(StompHeaders headers, Object payload) {
				received.add(new String((byte[]) payload, UTF_8));
			}
		});

		triggerAlert();

		String message = received.poll(5, SECONDS);
		assertThat(message).isNotNull();
		assertThat(message).contains(WATCHED_PLATE);
		session.disconnect();
	}

	@Test
	void connectWithoutTokenIsRejected() {
		assertThatThrownBy(() -> connect(null)).isInstanceOf(ExecutionException.class);
	}

	@Test
	void connectWithInvalidTokenIsRejected() {
		assertThatThrownBy(() -> connect("not-a-jwt")).isInstanceOf(ExecutionException.class);
	}

	private void triggerAlert() {
		client.post().uri("/api/v1/watchlist")
				.contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("plate", WATCHED_PLATE, "reason", "ROBBERY"))
				.retrieve().toBodilessEntity();

		Long cameraId = createCamera("Cam");
		Map<String, Object> detection = new HashMap<>();
		detection.put("plate", WATCHED_PLATE);
		detection.put("detectedAt", "2026-07-18T10:00:00Z");
		cameraClient(mintApiKey(cameraId)).post().uri("/api/v1/detections")
				.contentType(MediaType.APPLICATION_JSON)
				.body(detection)
				.retrieve().toBodilessEntity();
	}
}
