package com.sentinela.alpr.detections.api;

import java.util.List;

public record DetectionPage(
		List<DetectionResponse> content,
		String nextCursor) {
}
