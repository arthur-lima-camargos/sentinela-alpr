package com.sentinela.alpr.detections.infra;

import java.time.Instant;
import java.util.List;

import com.sentinela.alpr.detections.domain.Detection;

public interface DetectionRepositoryCustom {

	List<Detection> findPage(String plate, Long cameraId, Instant from, Instant to,
			Instant cursorTs, Long cursorId, int limit);
}
