package com.sentinela.alpr.detections.api;

import java.net.URI;
import java.time.Instant;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sentinela.alpr.detections.domain.DetectionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/detections")
class DetectionController {

	private final DetectionService service;

	DetectionController(DetectionService service) {
		this.service = service;
	}

	@PostMapping
	ResponseEntity<DetectionResponse> record(
			@AuthenticationPrincipal Long cameraId,
			@Valid @RequestBody DetectionRequest request) {
		DetectionResponse created = service.record(cameraId, request);
		return ResponseEntity.created(URI.create("/api/v1/detections/" + created.id())).body(created);
	}

	@GetMapping
	DetectionPage query(
			@RequestParam(required = false) String plate,
			@RequestParam(required = false) Long cameraId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
			@RequestParam(required = false) String cursor,
			@RequestParam(required = false) Integer size) {
		return service.query(plate, cameraId, from, to, cursor, size);
	}
}
