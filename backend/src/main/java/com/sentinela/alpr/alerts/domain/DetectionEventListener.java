package com.sentinela.alpr.alerts.domain;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.sentinela.alpr.alerts.infra.AlertBroadcaster;
import com.sentinela.alpr.detections.domain.DetectionRecordedEvent;
import com.sentinela.alpr.watchlist.domain.WatchlistService;

@Component
class DetectionEventListener {

	private final WatchlistService watchlist;
	private final AlertService alerts;
	private final AlertBroadcaster broadcaster;

	DetectionEventListener(WatchlistService watchlist, AlertService alerts, AlertBroadcaster broadcaster) {
		this.watchlist = watchlist;
		this.alerts = alerts;
		this.broadcaster = broadcaster;
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	void onDetectionRecorded(DetectionRecordedEvent event) {
		watchlist.findActiveMatch(event.plate()).ifPresent(match ->
				alerts.createFromDetection(event, match.id())
						.ifPresent(broadcaster::broadcast));
	}
}
