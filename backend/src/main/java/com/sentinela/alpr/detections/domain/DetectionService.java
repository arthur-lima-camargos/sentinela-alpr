package com.sentinela.alpr.detections.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sentinela.alpr.cameras.api.CameraResponse;
import com.sentinela.alpr.cameras.domain.CameraService;
import com.sentinela.alpr.detections.api.DetectionPage;
import com.sentinela.alpr.detections.api.DetectionRequest;
import com.sentinela.alpr.detections.api.DetectionResponse;
import com.sentinela.alpr.detections.infra.DetectionRepository;
import com.sentinela.alpr.shared.error.BusinessRuleException;
import com.sentinela.alpr.shared.error.NotFoundException;
import com.sentinela.alpr.shared.event.DomainEventPublisher;
import com.sentinela.alpr.shared.support.PlateNormalizer;

@Service
public class DetectionService {

	static final int MAX_PAGE_SIZE = 200;
	static final int DEFAULT_PAGE_SIZE = 20;

	private final DetectionRepository repository;
	private final CameraService cameraService;
	private final DomainEventPublisher events;

	DetectionService(DetectionRepository repository, CameraService cameraService, DomainEventPublisher events) {
		this.repository = repository;
		this.cameraService = cameraService;
		this.events = events;
	}

	@Transactional
	public DetectionResponse record(DetectionRequest request) {
		String plate = normalizePlate(request.plate());

		CameraResponse camera = cameraService.findById(request.cameraId())
				.orElseThrow(() -> new NotFoundException("Camera not found: " + request.cameraId()));
		if (!camera.active()) {
			throw new BusinessRuleException("Inactive Camera: " + request.cameraId());
		}

		Detection saved = repository.save(new Detection(plate, request.cameraId(), request.detectedAt()));
		events.publish(new DetectionRecordedEvent(saved.getId(), saved.getPlate(),
				saved.getCameraId(), saved.getDetectedAt()));
		return toResponse(saved);
	}

	@Transactional(readOnly = true)
	public DetectionPage query(String plate, Long cameraId, Instant from, Instant to, String cursor, Integer size) {
		int pageSize = normalizeSize(size);
		String normalizedPlate = plate == null ? null : PlateNormalizer.normalize(plate);
		DetectionCursor anchor = cursor == null ? null : DetectionCursor.decode(cursor);

		List<Detection> rows = repository.findPage(
				normalizedPlate, cameraId, from, to,
				anchor == null ? null : anchor.detectedAt(),
				anchor == null ? null : anchor.id(),
				pageSize + 1);

		String nextCursor = null;
		if (rows.size() > pageSize) {
			Detection last = rows.get(pageSize - 1);
			nextCursor = new DetectionCursor(last.getDetectedAt(), last.getId()).encode();
			rows = rows.subList(0, pageSize);
		}

		List<DetectionResponse> content = new ArrayList<>(rows.size());
		for (Detection d : rows) {
			content.add(toResponse(d));
		}
		return new DetectionPage(content, nextCursor);
	}

	private static int normalizeSize(Integer size) {
		if (size == null || size <= 0) {
			return DEFAULT_PAGE_SIZE;
		}
		return Math.min(size, MAX_PAGE_SIZE);
	}

	private static String normalizePlate(String raw) {
		String plate = PlateNormalizer.normalize(raw);
		if (!PlateNormalizer.isValid(plate)) {
			throw new BusinessRuleException("Plate is invalid: " + raw);
		}
		return plate;
	}

	private static DetectionResponse toResponse(Detection d) {
		return new DetectionResponse(d.getId(), d.getPlate(), d.getCameraId(), d.getDetectedAt());
	}
}
