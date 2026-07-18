package com.sentinela.alpr.alerts.infra;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.sentinela.alpr.alerts.api.AlertResponse;

@Component
public class AlertBroadcaster {

	static final String TOPIC = "/topic/alerts";

	private final SimpMessagingTemplate messaging;

	AlertBroadcaster(SimpMessagingTemplate messaging) {
		this.messaging = messaging;
	}

	public void broadcast(AlertResponse alert) {
		messaging.convertAndSend(TOPIC, alert);
	}
}
