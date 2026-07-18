package com.sentinela.alpr.shared.event;

public interface DomainEventPublisher {

	void publish(Object event);
}
