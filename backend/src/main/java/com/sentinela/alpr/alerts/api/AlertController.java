package com.sentinela.alpr.alerts.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sentinela.alpr.alerts.domain.AlertService;
import com.sentinela.alpr.alerts.domain.AlertStatus;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/alerts")
class AlertController {

	private final AlertService service;

	AlertController(AlertService service) {
		this.service = service;
	}

	@GetMapping
	Page<AlertResponse> list(
			@RequestParam(required = false) AlertStatus status,
			@PageableDefault(size = 20) Pageable pageable) {
		return service.list(status, pageable);
	}

	@GetMapping("/summary")
	AlertSummaryResponse summary() {
		return service.summary();
	}

	@PatchMapping("/{id}")
	AlertResponse updateStatus(@PathVariable Long id, @Valid @RequestBody AlertStatusRequest request) {
		return service.updateStatus(id, request.status());
	}
}
