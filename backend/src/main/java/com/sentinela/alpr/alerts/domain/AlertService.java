package com.sentinela.alpr.alerts.domain;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.sentinela.alpr.alerts.api.AlertResponse;
import com.sentinela.alpr.alerts.api.AlertSummaryResponse;
import com.sentinela.alpr.alerts.infra.AlertRepository;
import com.sentinela.alpr.detections.domain.DetectionRecordedEvent;
import com.sentinela.alpr.shared.error.NotFoundException;

@Service
public class AlertService {

	private final AlertRepository repository;

	AlertService(AlertRepository repository) {
		this.repository = repository;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Optional<AlertResponse> createFromDetection(DetectionRecordedEvent event, Long watchedVehicleId) {
		if (repository.existsByDetectionId(event.detectionId())) {
			return Optional.empty();
		}
		Alert saved = repository.save(
				new Alert(event.plate(), event.detectionId(), watchedVehicleId, event.detectedAt()));
		return Optional.of(toResponse(saved));
	}

	@Transactional(readOnly = true)
	public Page<AlertResponse> list(AlertStatus status, Pageable pageable) {
		Page<Alert> page = status == null
				? repository.findAll(pageable)
				: repository.findByStatus(status, pageable);
		return page.map(AlertService::toResponse);
	}

	@Transactional(readOnly = true)
	public AlertSummaryResponse summary() {
		return new AlertSummaryResponse(
				repository.countByStatus(AlertStatus.NEW),
				repository.countByStatus(AlertStatus.SEEN));
	}

	@Transactional
	public AlertResponse updateStatus(Long id, AlertStatus status) {
		Alert alert = repository.findById(id)
				.orElseThrow(() -> new NotFoundException("Alert not found: " + id));
		alert.setStatus(status);
		return toResponse(alert);
	}

	private static AlertResponse toResponse(Alert a) {
		return new AlertResponse(a.getId(), a.getPlate(), a.getDetectionId(), a.getWatchedVehicleId(),
				a.getDetectedAt(), a.getStatus(), a.getCreatedAt());
	}
}
