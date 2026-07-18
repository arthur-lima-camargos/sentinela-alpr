package com.sentinela.alpr.shared.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
class SpringDomainEventPublisher implements DomainEventPublisher {

	private final ApplicationEventPublisher delegate;

	SpringDomainEventPublisher(ApplicationEventPublisher delegate) {
		this.delegate = delegate;
	}

	@Override
	public void publish(Object event) {
		delegate.publishEvent(event);
	}
}
